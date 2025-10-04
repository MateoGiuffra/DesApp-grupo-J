package com.desapp.football_api.service;

import com.desapp.football_api.model.Match;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.utils.ScrapeHelper;
import com.desapp.football_api.utils.WhoScoredLink;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class MatchService {

    @Autowired
    private WhoScoredService whoScoredService;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamService teamService;
    @Autowired
    private MatchRepository matchRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yy", Locale.US);

    public List<Match> getUpcomingMatches(Long teamId) {
        try {
            Team team = ScrapeHelper.getOrScrape(
                    () -> teamRepository.findById(teamId).orElse(null),
                    Objects::isNull,
                    () -> teamService.scrapeTeamByIdAndType(teamId, StatsType.Current)
            );

            String url = WhoScoredLink.getTeamFixturesLink(teamId);
            String body = whoScoredService.fetchJSONString(url);

            List<Match> matches = parseFixtures(body, team);

            // Persist: replace existing stored fixtures for this team
            matchRepository.deleteByTeam_Id(teamId);
            return matchRepository.saveAll(matches);
        } catch (Exception e) {

            // In case of parsing network errors, return empty list to avoid breaking API
            return List.of();
        }
    }

    private List<Match> parseFixtures(String json, Team team) throws Exception {
        List<Match> result = new ArrayList<>();
        if (json == null || json.isBlank()) return result;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        // The endpoint returns a raw array of arrays OR wrapped. Try to find the array node.
        JsonNode arrayNode = root.isArray() ? root : (root.elements().hasNext() ? root.elements().next() : null);
        if (arrayNode == null || !arrayNode.isArray()) return result;

        LocalDateTime now = LocalDateTime.now();

        for (JsonNode node : arrayNode) {
            if (!node.isArray()) continue;

            Long id = getLong(node, 0);
            String dateStr = getText(node, 2);
            String timeStr = getText(node, 3);
            Long homeId = getLong(node, 4);
            String homeName = getText(node, 5);
            Long awayId = getLong(node, 7);
            String awayName = getText(node, 8);
            String competition = getText(node, 16);

            if (id == null || dateStr == null || timeStr == null) continue;

            timeStr = timeStr.replace(" ", ""); // normalize e.g., "18: 00" -> "18:00"

            LocalDate date;
            LocalTime time;
            try {
                date = LocalDate.parse(dateStr, DATE_FMT);
                time = LocalTime.parse(timeStr);
            } catch (Exception ex) {
                continue; // skip malformed rows
            }
            LocalDateTime matchDateTime = LocalDateTime.of(date, time);
            if (matchDateTime.isBefore(now)) continue; // only upcoming

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
        }
        return result;
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
