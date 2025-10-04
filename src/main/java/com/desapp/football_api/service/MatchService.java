package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.TeamNotFoundException;
import com.desapp.football_api.model.Match;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.utils.ScrapeHelper;
import com.desapp.football_api.utils.WhoScoredLink;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    @Autowired
    private WhoScoredService whoScoredService;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamService teamService;
    @Autowired
    private MatchRepository matchRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yy", Locale.US);

    public Boolean hasToScrap(Team team) {
        return team == null;
    }

    @Transactional
    public List<Match> getUpcomingMatches(Long teamId) {
        log.info("[Matches] Solicitando próximos partidos para teamId={}", teamId);
        try {
            Team team = ScrapeHelper.getOrScrape(
                    () -> {
                        Team t = teamRepository.findById(teamId).orElse(null);
                        log.info("[Matches] Resultado DB teamId={} -> {}", teamId, t == null ? "null" : (t.getName() + " (#" + t.getId() + ")"));
                        return t;
                    },
                    this::hasToScrap,
                    () -> {
                        log.info("[Matches] Ejecutando scrape de Team {} porque no existe en DB", teamId);
                        return teamService.scrapeTeamByIdAndType(teamId, StatsType.Current);
                    }
            );

            String url = WhoScoredLink.getTeamFixturesLink(teamId);
            log.info("[Matches] URL fixtures: {}", url);
            String body = whoScoredService.fetchJSONString(url);
            log.info("[Matches] Respuesta fixtures: length={} snippet='{}'", body == null ? 0 : body.length(), body == null ? "null" : body.substring(0, Math.min(200, body.length())).replaceAll("\n", " "));

            List<Match> matches = parseFixtures(body, team);
            log.info("[Matches] Partidos parseados (antes de persistir): {}", matches.size());

            // Persist: replace existing stored fixtures for this team
            try {
                matchRepository.deleteByTeam_Id(teamId);
            } catch (Exception ex) {
                log.warn("[Matches] Error al eliminar fixtures previos de teamId={}: {}", teamId, ex.getMessage());
            }

            List<Match> saved = matchRepository.saveAll(matches);
            log.info("[Matches] Partidos guardados para teamId={} -> {}", teamId, saved.size());
            return saved;
        } catch (TeamNotFoundException teamNotFoundException) {
            throw teamNotFoundException;
        } catch (Exception e) {
            log.error("[Matches] Error al obtener próximos partidos para teamId={}: {}", teamId, e.getMessage(), e);
            // In case of parsing network errors, return empty list to avoid breaking API
            return List.of();
        }
    }


    private List<Match> parseFixtures(String json, Team team) throws Exception {
        List<Match> result = new ArrayList<>();
        if (json == null || json.isBlank()) {
            log.warn("[Matches] Cuerpo vacío o nulo al parsear fixtures");
            return result;
        }

        // Algunos endpoints de WhoScored devuelven un pseudo-JSON (listas separadas por coma, con comillas simples y sin un array raíz).
        String normalized = normalizeFixturesPayload(json);
        log.info("[Matches] Normalized JSON length={} snippet='{}'", normalized.length(), normalized.substring(0, Math.min(200, normalized.length())).replaceAll("\n", " "));

        // En lugar de depender 100% de JSON válido, extraemos cada fila [ ... ] con regex y parse propio.
        java.util.regex.Pattern rowPattern = java.util.regex.Pattern.compile("\\[(?:[^\\[\\]])*\\]");
        java.util.regex.Matcher matcher = rowPattern.matcher(normalized);

        LocalDateTime now = LocalDateTime.now();
        int total = 0, processed = 0, skippedPast = 0, skippedMalformed = 0;
        while (matcher.find()) {
            String row = matcher.group();
            total++;
            List<String> tokens = splitRowKeepingQuotes(row);
            try {
                Long id = parseLong(tokens, 0);
                String dateStr = parseString(tokens, 2);
                String timeStr = parseString(tokens, 3);
                Long homeId = parseLong(tokens, 4);
                String homeName = parseString(tokens, 5);
                Long awayId = parseLong(tokens, 7);
                String awayName = parseString(tokens, 8);
                String competition = parseString(tokens, 16);

                if (id == null || dateStr == null || timeStr == null) {
                    skippedMalformed++;
                    continue;
                }

                String originalTime = timeStr;
                timeStr = timeStr.replace(" ", "");
                LocalDate date = LocalDate.parse(dateStr, DATE_FMT);
                LocalTime time = timeStr.length() >= 5 ? LocalTime.parse(timeStr.substring(0, 5)) : LocalTime.parse(timeStr);
                LocalDateTime matchDateTime = LocalDateTime.of(date, time);
                if (matchDateTime.isBefore(now)) {
                    skippedPast++;
                    continue;
                }

                Match m = new Match();
                m.setId(id);
                m.setDate(dateStr);
                m.setTime(timeStr);
                m.setHomeTeamId(homeId);
                m.setHomeTeamName(homeName);
                m.setAwayTeamId(awayId);
                m.setAwayTeamName(awayName);
                m.setCompetition(competition);
                m.setTeam(team);
                result.add(m);
                processed++;
            } catch (Exception ex) {
                skippedMalformed++;
            }
        }
        log.info("[Matches] Parse resumen -> total:{} guardados:{} pasados:{} malformados:{}", total, processed, skippedPast, skippedMalformed);
        return result;
    }

    private List<String> splitRowKeepingQuotes(String row) {
        if (row == null) return List.of();
        String s = row.trim();
        if (s.startsWith("[")) s = s.substring(1);
        if (s.endsWith("]")) s = s.substring(0, s.length() - 1);
        List<String> tokens = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue; // no incluir las comillas
            }
            if (c == ',' && !inQuotes) {
                tokens.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) tokens.add(cur.toString().trim());
        return tokens;
    }

    private Long parseLong(List<String> tokens, int idx) {
        if (tokens == null || idx < 0 || idx >= tokens.size()) return null;
        String t = tokens.get(idx);
        if (t == null) return null;
        t = t.trim();
        if (t.isEmpty()) return null;
        // quitar posibles comillas
        if ((t.startsWith("\"") && t.endsWith("\"")) || (t.startsWith("'") && t.endsWith("'"))) {
            t = t.substring(1, t.length() - 1);
        }
        try {
            return Long.parseLong(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseString(List<String> tokens, int idx) {
        if (tokens == null || idx < 0 || idx >= tokens.size()) return null;
        String t = tokens.get(idx);
        if (t == null) return null;
        t = t.trim();
        if ((t.startsWith("\"") && t.endsWith("\"")) || (t.startsWith("'") && t.endsWith("'"))) {
            t = t.substring(1, t.length() - 1);
        }
        return t.isEmpty() ? null : t;
    }

    private String normalizeFixturesPayload(String raw) {
        if (raw == null) return "[]";
        String s = raw.trim();
        // Quitar posibles caracteres iniciales como comas o BOM
        while (!s.isEmpty() && (s.charAt(0) == ',' || Character.isWhitespace(s.charAt(0)))) {
            s = s.substring(1);
        }
        List<String> parts = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0 && start >= 0) {
                    parts.add(s.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        String jsonArray;
        if (parts.isEmpty()) {
            // Si ya parece un array completo, úsalo tal cual
            jsonArray = s.startsWith("[") ? s : "[]";
        } else {
            jsonArray = "[" + String.join(",", parts) + "]";
        }
        // Convertir comillas simples a dobles para formar JSON válido
        jsonArray = jsonArray.replace("'", "\"");
        log.info("[Matches] Normalización: grupos de fixtures detectados={}", parts.size());
        return jsonArray;
    }

    private JsonNode findArrayOfArrays(JsonNode root) {
        if (root == null) return null;
        if (root.isArray()) {
            for (JsonNode n : root) if (n.isArray()) return root;
        }
        if (root.isObject()) {
            var it = root.fieldNames();
            while (it.hasNext()) {
                String key = it.next();
                JsonNode child = root.get(key);
                JsonNode candidate = findArrayOfArrays(child);
                if (candidate != null) return candidate;
            }
        } else {
            Iterator<JsonNode> it = root.elements();
            while (it.hasNext()) {
                JsonNode child = it.next();
                JsonNode candidate = findArrayOfArrays(child);
                if (candidate != null) return candidate;
            }
        }
        return null;
    }

    private static Long getLong(JsonNode arr, int idx) {
        JsonNode n = arr.size() > idx ? arr.get(idx) : null;
        if (n == null) return null;
        if (n.isNumber()) return n.asLong();
        if (n.isTextual()) {
            try {
                return Long.parseLong(n.asText().trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static String getText(JsonNode arr, int idx) {
        JsonNode n = arr.size() > idx ? arr.get(idx) : null;
        if (n == null || n.isNull()) return null;
        String val = n.asText();
        return val == null ? null : val.trim();
    }
}
