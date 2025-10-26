package com.desapp.football_api.utils;

import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.match.Match;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WhoScoredHelper {

    private static final Map<String, String> POSITION_MAP;

    private WhoScoredHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("GK", "Goalkeeper");
        map.put("DF", "Defender");
        map.put("D", "Defender"); // fallback
        map.put("DM", "Defensive Midfielder");
        map.put("M", "Midfielder");
        map.put("AM", "Attacking Midfielder"); // Mediapunta
        map.put("FW", "Forward");
        map.put("F", "Forward"); // fallback
        map.put("ST", "Striker");
        POSITION_MAP = Collections.unmodifiableMap(map);
    }

    private static final Map<String, String> SIDE_MAP = Map.of(
            "L", "Left",
            "R", "Right",
            "C", "Centre"
    );

    public static String parsePlayedPositions(String raw) {
        if (raw == null || raw.isBlank()) return "";

        String[] tokens = raw.replaceAll("^-|-$", "").split("-");
        List<String> results = new ArrayList<>();

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            String roleKey = POSITION_MAP.keySet().stream()
                    .filter(token::startsWith)
                    .max(Comparator.comparingInt(String::length))
                    .orElse(null);

            if (roleKey == null) {
                results.add("Unknown(" + token + ")");
                continue;
            }

            String pos = POSITION_MAP.get(roleKey);
            String sides = token.substring(roleKey.length());

            if (sides.isEmpty()) {
                results.add(pos);
                continue;
            }

            List<String> sideList = sides.chars()
                    .mapToObj(c -> SIDE_MAP.getOrDefault(String.valueOf((char) c), ""))
                    .filter(s -> !s.isEmpty())
                    .toList();

            results.add(!sideList.isEmpty()
                    ? pos + " (" + String.join(", ", sideList) + ")"
                    : pos);
        }
        return String.join(", ", results);
    }

    public static String calculateBirthDateByAge(int age) {
        LocalDate today = java.time.LocalDate.now();
        LocalDate birthDate = today.minusYears(age);
        return birthDate.getDayOfMonth() + "/" + birthDate.getMonthValue() + "/" + birthDate.getYear();
    }

    public static String getCountryNameFromCode(String code) {
        if (code == null || code.isBlank()) return "Unknown";
        Locale locale = new Locale("", code.toUpperCase());
        String countryName = locale.getDisplayCountry(Locale.ENGLISH);
        return countryName.equalsIgnoreCase(code) ? "Unknown" : countryName;
    }

    public static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0 + 0.0001) / 100.0;
    }

    public static List<Long> getIdsFromResponse(String response) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        JsonNode playerTableStats = root.get("playerTableStats");
        if (playerTableStats == null || !playerTableStats.isArray()) {
            return Collections.emptyList();
        }
        Set<Long> ids = HashSet.newHashSet(Math.max(16, playerTableStats.size()));
        for (JsonNode playerNode : playerTableStats) {
            JsonNode idNode = playerNode.get("playerId");
            if (idNode != null && idNode.canConvertToLong()) {
                ids.add(idNode.asLong());
            }
        }
        return new ArrayList<>(ids);
    }


    public static List<Match> parseFixtures(String payload, Team team) {
        List<List<String>> parsed = ParserUtil.parseFixturesPayload(payload);
        List<Match> result = new ArrayList<>();

        for (List<String> matchInfo : parsed) {
            Match match = createMatchFromFixture(matchInfo, team);
            if (match == null) continue;
            result.add(match);
        }

        return result;
    }

    private static Match createMatchFromFixture(List<String> matchInfo, Team team) {
        if (matchInfo == null || matchInfo.isEmpty()) return null;

        Long id = getLong(matchInfo, 0);
        String dateStr = getText(matchInfo, 2);
        String timeStr = getText(matchInfo, 3);
        Long homeId = getLong(matchInfo, 4);
        String homeName = getText(matchInfo, 5);
        Long awayId = getLong(matchInfo, 7);
        String awayName = getText(matchInfo, 8);
        String competition = getCompetition(matchInfo);

        if (id == null || dateStr == null || timeStr == null) return null;

        timeStr = timeStr.replace(" ", "");

        return new Match(id, dateStr, timeStr, homeId, homeName, awayId, awayName, competition, team);
    }

    private static String getCompetition(List<String> matchInfo) {
        AtomicInteger i = new AtomicInteger(-1);
        matchInfo.forEach(word -> {
            if (word != null && word.contains("/")) {
                i.set(matchInfo.indexOf(word) + 1);
            }
        });
        String raw;
        if (i.get() >= 0 && i.get() < matchInfo.size()) {
            raw = matchInfo.get(i.get());
        } else if (matchInfo.size() > 16) {
            raw = matchInfo.get(16); // fallback: typical index for competition name
        } else {
            return null;
        }
        return raw == null ? null : raw.replace('"', ' ').replace('\'', ' ').trim();

    }

    private static Long getLong(List<String> arr, int idx) {
        try {
            return Long.valueOf(arr.get(idx).trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String getText(List<String> arr, int idx) {
        if (arr.size() <= idx) return null;
        String v = arr.get(idx);
        if (v == null) return null;
        // remove both double and single quotes, then trim
        return v.replace('"', ' ').replace('\'', ' ').trim();
    }

}
