package com.desapp.football_api.utils;

import com.desapp.football_api.model.Match;
import com.desapp.football_api.model.Team;
import com.fasterxml.jackson.core.JsonParser;
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
        Set<Long> ids = new HashSet<>(Math.max(16, playerTableStats.size()));
        for (JsonNode playerNode : playerTableStats) {
            JsonNode idNode = playerNode.get("playerId");
            if (idNode != null && idNode.canConvertToLong()) {
                ids.add(idNode.asLong());
            }
        }
        return new ArrayList<>(ids);
    }


    public static List<Match> parseFixtures(String payload, Team team) throws Exception {
        System.out.println("[DEBUG_LOG][parseFixtures] entry. teamId=" + (team != null ? team.getId() : null));
        List<Match> result = new ArrayList<>();
        if (payload == null || payload.isBlank()) {
            System.out.println("[DEBUG_LOG][parseFixtures] payload is null/blank");
            return result;
        }
        System.out.println("[DEBUG_LOG][parseFixtures] payload length=" + payload.length());
        System.out.println("[DEBUG_LOG][parseFixtures] payload head=" + payload.substring(0, Math.min(payload.length(), 300)) + (payload.length() > 300 ? "..." : ""));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        JsonNode root = null;
        JsonNode arrayNode = null;
        try {
            // Try to parse as JSON first
            root = mapper.readTree(payload);
            System.out.println("[DEBUG_LOG][parseFixtures] root parsed. type=" + (root == null ? "null" : (root.isArray() ? "array" : (root.isObject() ? "object" : root.getNodeType()))));
            if (root != null) {
                if (root.isArray()) {
                    arrayNode = root;
                    System.out.println("[DEBUG_LOG][parseFixtures] using root array. size=" + root.size());
                } else if (root.isObject()) {
                    // Common key from WhoScored teamsfeed
                    JsonNode fixtures = root.get("fixtures");
                    if (fixtures != null && fixtures.isArray()) {
                        arrayNode = fixtures;
                        System.out.println("[DEBUG_LOG][parseFixtures] found 'fixtures' key. size=" + fixtures.size());
                    } else {
                        // Fallback: scan for the first array-of-arrays field
                        arrayNode = findFirstArrayOfArrays(root);
                        System.out.println("[DEBUG_LOG][parseFixtures] fallback findFirstArrayOfArrays -> " + (arrayNode == null ? "null" : (arrayNode.isArray() ? ("array size=" + arrayNode.size()) : arrayNode.getNodeType())));
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("[DEBUG_LOG][parseFixtures] primary JSON parse failed: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            // If JSON parsing fails (e.g., raw array-like text), sanitize and retry
        }

        if (arrayNode == null) {
            // Sanitize incoming array string and parse again
            String sanitized = sanitizeArrayString(payload);
            System.out.println("[DEBUG_LOG][parseFixtures] arrayNode is null. Trying sanitize+parse. sanitized length=" + sanitized.length());
            try {
                JsonNode maybeArray = mapper.readTree(sanitized);
                System.out.println("[DEBUG_LOG][parseFixtures] after sanitize parse type=" + (maybeArray == null ? "null" : (maybeArray.isArray() ? "array" : maybeArray.getNodeType())));
                if (maybeArray != null && maybeArray.isArray()) {
                    arrayNode = maybeArray;
                } else {
                    // Aggressive fallback: force JSON by replacing single quotes with double quotes and cleaning commas
                    String aggressive = payload.replace("\r", "").replace("\n", "");
                    aggressive = aggressive.replaceAll(",\\s*]", "]");
                    aggressive = aggressive.replaceAll(",\\s*}", "}");
                    aggressive = aggressive.replaceAll(",\\s*,+", ",");
                    aggressive = aggressive.replace('\'', '"');
                    if (!aggressive.startsWith("[")) aggressive = "[" + aggressive + "]";
                    long opens = aggressive.chars().filter(c -> c == '[').count();
                    long closes = aggressive.chars().filter(c -> c == ']').count();
                    while (closes < opens) {
                        aggressive += "]";
                        closes++;
                    }
                    System.out.println("[DEBUG_LOG][parseFixtures] trying aggressive sanitize. length=" + aggressive.length());
                    try {
                        JsonNode maybeArray2 = mapper.readTree(aggressive);
                        System.out.println("[DEBUG_LOG][parseFixtures] aggressive parse type=" + (maybeArray2 == null ? "null" : (maybeArray2.isArray() ? "array" : maybeArray2.getNodeType())));
                        if (maybeArray2 != null && maybeArray2.isArray()) {
                            arrayNode = maybeArray2;
                        }
                    } catch (Exception e2) {
                        System.out.println("[DEBUG_LOG][parseFixtures] aggressive parse failed: " + e2.getClass().getSimpleName() + " - " + e2.getMessage());
                    }
                }
            } catch (Exception e) {
                System.out.println("[DEBUG_LOG][parseFixtures] sanitize parse failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                // continue to aggressive below just in case
                String aggressive = payload.replace("\r", "").replace("\n", "");
                aggressive = aggressive.replaceAll(",\\s*]", "]");
                aggressive = aggressive.replaceAll(",\\s*}", "}");
                aggressive = aggressive.replaceAll(",\\s*,+", ",");
                aggressive = aggressive.replace('\'', '"');
                if (!aggressive.startsWith("[")) aggressive = "[" + aggressive + "]";
                long opens = aggressive.chars().filter(c -> c == '[').count();
                long closes = aggressive.chars().filter(c -> c == ']').count();
                while (closes < opens) {
                    aggressive += "]";
                    closes++;
                }
                System.out.println("[DEBUG_LOG][parseFixtures] trying aggressive sanitize (from catch). length=" + aggressive.length());
                try {
                    JsonNode maybeArray2 = mapper.readTree(aggressive);
                    System.out.println("[DEBUG_LOG][parseFixtures] aggressive parse type (from catch)=" + (maybeArray2 == null ? "null" : (maybeArray2.isArray() ? "array" : maybeArray2.getNodeType())));
                    if (maybeArray2 != null && maybeArray2.isArray()) {
                        arrayNode = maybeArray2;
                    }
                } catch (Exception e2) {
                    System.out.println("[DEBUG_LOG][parseFixtures] aggressive parse failed (from catch): " + e2.getClass().getSimpleName() + " - " + e2.getMessage());
                }
            }
        }

        if (arrayNode == null || !arrayNode.isArray()) {
            System.out.println("[DEBUG_LOG][parseFixtures] arrayNode is " + (arrayNode == null ? "null" : arrayNode.getNodeType()) + ". Trying manual parser...");
            List<Match> manual = parseFixturesManually(payload, team);
            System.out.println("[DEBUG_LOG][parseFixtures] manual parser produced size=" + manual.size());
            return manual;
        }

        System.out.println("[DEBUG_LOG][parseFixtures] fixtures array size=" + arrayNode.size());
        int idx = 0;
        for (JsonNode node : arrayNode) {
            if (!node.isArray()) {
                System.out.println("[DEBUG_LOG][parseFixtures] item " + idx + " is not an array: type=" + node.getNodeType());
                idx++;
                continue;
            }

            Long id = getLong(node, 0);
            String dateStr = getText(node, 2);
            String timeStr = getText(node, 3);
            Long homeId = getLong(node, 4);
            String homeName = getText(node, 5);
            Long awayId = getLong(node, 7);
            String awayName = getText(node, 8);
            String competition = getText(node, 16);

            if (id == null || dateStr == null || timeStr == null) {
                System.out.println("[DEBUG_LOG][parseFixtures] skip item " + idx + " due to nulls: id=" + id + ", date=" + dateStr + ", time=" + timeStr);
                idx++;
                continue;
            }

            if (timeStr != null) {
                timeStr = timeStr.replace(" ", ""); // normaliza e.g., "18: 00" -> "18:00"
            }

            Match m = Match.builder()
                    .id(id)
                    .date(dateStr)
                    .time(timeStr)
                    .homeTeamId(homeId)
                    .homeTeamName(homeName)
                    .awayTeamId(awayId)
                    .awayTeamName(awayName)
                    .competition(competition)
                    .team(team)
                    .build();

            System.out.println("[DEBUG_LOG][parseFixtures] built match idx=" + idx + " id=" + id + " " + homeName + " vs " + awayName + " date=" + dateStr + " " + timeStr + " comp=" + competition);
            result.add(m);
            idx++;
        }
        System.out.println("[DEBUG_LOG][parseFixtures] returning matches size=" + result.size());
        if (!result.isEmpty()) {
            Match m0 = result.get(0);
            System.out.println("[DEBUG_LOG][parseFixtures] first match sample: id=" + m0.getId() + ", " + m0.getHomeTeamName() + " vs " + m0.getAwayTeamName() + ", date=" + m0.getDate() + " " + m0.getTime());
        }
        return result;
    }

    private static List<Match> parseFixturesManually(String payload, Team team) {
        System.out.println("[DEBUG_LOG][manualParser] start. payload length=" + (payload == null ? -1 : payload.length()));
        List<Match> out = new ArrayList<>();
        if (payload == null) return out;
        String s = payload;
        int depth = 0;
        boolean inQuote = false;
        char quote = '\0';
        StringBuilder token = null;
        List<String> row = null;
        List<List<String>> rows = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inQuote) {
                if (c == quote) {
                    inQuote = false;
                } else {
                    if (depth == 2 && token != null) token.append(c);
                }
                continue;
            }
            if (c == '\'' || c == '"') {
                inQuote = true; quote = c;
                if (depth == 2 && token == null) token = new StringBuilder();
                continue;
            }
            if (c == '[') {
                depth++;
                if (depth == 2) {
                    row = new ArrayList<>();
                    token = new StringBuilder();
                }
                continue;
            }
            if (c == ']') {
                if (depth == 2) {
                    if (token == null) token = new StringBuilder();
                    row.add(token.toString().trim());
                    rows.add(row);
                    row = null;
                    token = null;
                }
                depth--;
                continue;
            }
            if (c == ',') {
                if (depth == 2) {
                    if (token == null) token = new StringBuilder();
                    row.add(token.toString().trim());
                    token = new StringBuilder();
                }
                continue;
            }
            if (depth == 2) {
                if (token == null) token = new StringBuilder();
                token.append(c);
            }
        }
        System.out.println("[DEBUG_LOG][manualParser] rows detected=" + rows.size());
        // Build matches
        int idx = 0;
        for (List<String> cols : rows) {
            try {
                Long id = toLong(cols, 0);
                String dateStr = toText(cols, 2);
                String timeStr = toText(cols, 3);
                Long homeId = toLong(cols, 4);
                String homeName = toText(cols, 5);
                Long awayId = toLong(cols, 7);
                String awayName = toText(cols, 8);
                String competition = toText(cols, 16);

                if (id == null || dateStr == null || timeStr == null) {
                    System.out.println("[DEBUG_LOG][manualParser] skip row " + idx + " due to nulls: id=" + id + ", date=" + dateStr + ", time=" + timeStr + " (cols size=" + cols.size() + ")");
                    idx++;
                    continue;
                }
                if (timeStr != null) timeStr = timeStr.replace(" ", "");

                Match m = Match.builder()
                        .id(id)
                        .date(dateStr)
                        .time(timeStr)
                        .homeTeamId(homeId)
                        .homeTeamName(homeName)
                        .awayTeamId(awayId)
                        .awayTeamName(awayName)
                        .competition(competition)
                        .team(team)
                        .build();
                out.add(m);
            } catch (Exception ex) {
                System.out.println("[DEBUG_LOG][manualParser] error building row " + idx + ": " + ex.getMessage());
            }
            idx++;
        }
        System.out.println("[DEBUG_LOG][manualParser] built matches size=" + out.size());
        return out;
    }

    private static Long toLong(List<String> cols, int idx) {
        if (cols == null || idx >= cols.size()) return null;
        String v = cols.get(idx);
        if (v == null) return null;
        v = v.trim();
        if (v.isEmpty()) return null;
        try { return Long.parseLong(v); } catch (Exception ignored) { return null; }
    }

    private static String toText(List<String> cols, int idx) {
        if (cols == null || idx >= cols.size()) return null;
        String v = cols.get(idx);
        if (v == null) return null;
        v = v.trim();
        return v.isEmpty() ? null : v;
    }

    private static JsonNode findFirstArrayOfArrays(JsonNode obj) {
        if (obj == null || !obj.isObject()) return null;
        Iterator<String> fieldNames = obj.fieldNames();
        while (fieldNames.hasNext()) {
            String name = fieldNames.next();
            JsonNode value = obj.get(name);
            if (value != null) {
                if (value.isArray() && value.size() > 0 && value.get(0).isArray()) {
                    return value;
                }
                if (value.isObject()) {
                    JsonNode nested = findFirstArrayOfArrays(value);
                    if (nested != null) return nested;
                }
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

    // Makes a WhoScored-like array-of-arrays string parseable by Jackson
    private static String sanitizeArrayString(String s) {
        if (s == null) return "[]";
        String t = s.trim();

        // If the whole payload is quoted, unquote it
        if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
            t = t.substring(1, t.length() - 1);
        }

        // Remove line breaks and excessive spaces around colon in times like "19: 30"
        t = t.replace("\r", "").replace("\n", "");

        // Remove trailing commas before closing brackets (both inner and outer arrays)
        t = t.replaceAll(",\\s*]", "]");
        t = t.replaceAll(",\\s*}", "}");
        // Collapse duplicate commas that sometimes appear in feeds
        t = t.replaceAll(",\\s*,+", ",");

        // Some sources might omit the outer brackets; wrap if missing
        if (!t.startsWith("[")) {
            t = "[" + t + "]";
        }

        // Balance brackets if the string is truncated
        long opens = t.chars().filter(c -> c == '[').count();
        long closes = t.chars().filter(c -> c == ']').count();
        while (closes < opens) {
            t += "]";
            closes++;
        }
        return t;
    }

}
