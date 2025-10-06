package com.desapp.football_api.utils;

import com.desapp.football_api.model.Match;
import com.desapp.football_api.model.Team;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.*;

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
        Set<Long> ids = HashSet.newHashSet(playerTableStats.size());
        for (JsonNode playerNode : playerTableStats) {
            JsonNode idNode = playerNode.get("playerId");
            if (idNode != null && idNode.canConvertToLong()) {
                ids.add(idNode.asLong());
            }
        }
        return new ArrayList<>(ids);
    }


    public static List<Match> parseFixtures(String json, Team team) throws Exception {
        List<Match> result = new ArrayList<>();
        if (json == null || json.isBlank()) return result;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        // The endpoint returns a raw array of arrays OR wrapped. Try to find the array node.
        JsonNode arrayNode = root.isArray() ? root : (root.elements().hasNext() ? root.elements().next() : null);
        if (arrayNode == null || !arrayNode.isArray()) return result;


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
