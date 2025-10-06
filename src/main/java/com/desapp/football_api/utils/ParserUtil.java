package com.desapp.football_api.utils;

import java.util.ArrayList;
import java.util.List;

public class ParserUtil {
    public static List<List<String>> parseNestedList(String input) {
        List<List<String>> result = new ArrayList<>();

        if (input == null || input.isBlank()) {
            return result;
        }

        // Reemplazar dobles comas por ",null," para no perder posiciones
        String cleaned = input
                .replaceAll(",(\\s*),", ", null,")
                .replaceAll("\\[,", "[null,")
                .replaceAll(",\\s*\\]", ", null]");

        // Quitar corchetes exteriores
        String trimmed = cleaned.trim();
        while (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }

        // Separar filas principales por "],[" respetando la estructura
        String[] rows = trimmed.split("\\],\\s*\\[");

        for (String row : rows) {
            List<String> parsedRow = new ArrayList<>();
            row = row.replaceAll("^\\[|\\]$", "");

            StringBuilder current = new StringBuilder();
            boolean inQuotes = false;

            for (int i = 0; i < row.length(); i++) {
                char c = row.charAt(i);

                if (c == '"' || c == '\'') {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    String value = current.toString().trim();
                    parsedRow.add(value.isEmpty() || value.equals("null") ? null : value);
                    current.setLength(0);
                    continue;
                }
                current.append(c);
            }

            // último valor
            String value = current.toString().trim();
            parsedRow.add(value.isEmpty() || value.equals("null") ? null : value);

            result.add(parsedRow);
        }

        for (List<String> row : result) {
            for (int i = 0; i < row.size(); i++) {
                String value = row.get(i);
                if (value != null) {
                    String unquoted = value.replace("\"", "").replace("'", "").trim();
                    if (unquoted.equalsIgnoreCase("vs")
                            && i + 2 < row.size()
                            && !isNumeric(row.get(i + 2))) {
                        System.out.println(value);
                        row.add(i + 2, "0");
                        i++; // Skip the newly added element
                    }
                }
            }
        }

        return result;
    }


    private static boolean isNumeric(String s) {
        if (s == null) return false;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


}
