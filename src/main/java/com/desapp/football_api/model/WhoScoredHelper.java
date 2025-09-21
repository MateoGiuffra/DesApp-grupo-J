package com.desapp.football_api.model;

import java.time.LocalDate;
import java.util.*;

public class WhoScoredHelper {

    private static final Map<String, String> POSITION_MAP = new LinkedHashMap<>() {{
        put("GK", "Goalkeeper");
        put("DF", "Defender");
        put("D", "Defender"); // fallback
        put("DM", "Defensive Midfielder");
        put("M", "Midfielder");
        put("AM", "Attacking Midfielder"); // Mediapunta
        put("FW", "Forward");
        put("F", "Forward"); // fallback
        put("ST", "Striker");
    }};

    private static final Map<String, String> SIDE_MAP = Map.of(
            "L", "Left",
            "R", "Right",
            "C", "Centre"
    );

    public static String parsePlayedPositions(String raw) {
        if (raw == null || raw.isBlank()) return "";

        // Quitar guiones al inicio/fin y dividir por "-"
        String[] tokens = raw.replaceAll("^-|-$", "").split("-");

        List<String> results = new ArrayList<>();
        for (String token : tokens) {
            if (token.isEmpty()) continue;

            // Buscar el rol más largo primero (AM, DM, FW, DF, ST...)
            String roleKey = POSITION_MAP.keySet().stream()
                    .filter(token::startsWith)
                    .max(Comparator.comparingInt(String::length)) // importante: elegir el match más largo
                    .orElse("");

            if (roleKey.isEmpty()) {
                results.add("Unknown(" + token + ")");
                continue;
            }

            String pos = POSITION_MAP.get(roleKey);
            String sides = token.substring(roleKey.length()); // resto (ej: L, R, C)

            if (!sides.isEmpty()) {
                List<String> sideList = new ArrayList<>();
                for (char c : sides.toCharArray()) {
                    String side = SIDE_MAP.getOrDefault(String.valueOf(c), "");
                    if (!side.isEmpty()) sideList.add(side);
                }
                if (!sideList.isEmpty()) {
                    results.add(pos + " (" + String.join(", ", sideList) + ")");
                } else {
                    results.add(pos);
                }
            } else {
                results.add(pos);
            }
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
