package com.desapp.football_api.model;

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

        String[] tokens = raw.replaceAll("^(?:-)|(?:-)$", "").split("-");
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


}
