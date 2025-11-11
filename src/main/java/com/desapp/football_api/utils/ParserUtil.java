package com.desapp.football_api.utils;

import java.util.ArrayList;
import java.util.List;

public class ParserUtil {
    private ParserUtil() {
    }

    /**
     * Parse the fixtures payload (nested arrays) into a List<List<String>> keeping positions.
     * Empty values are returned as null. Quotes are preserved (WhoScoredHelper strips them later).
     * This parser is intentionally simple and only cares about commas outside quotes.
     */
    public static List<List<String>> parseFixturesPayload(String payload) {
        List<List<String>> out = new ArrayList<>();
        if (payload == null || payload.isBlank()) return out;

        String s = payload.trim();
        // Remove outermost brackets if present
        if (s.startsWith("[") && s.endsWith("]")) {
            s = s.substring(1, s.length() - 1);
        }
        //

        List<String> currentRow = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'' || c == '"') {
                inQuotes = !inQuotes;
                token.append(c);
                continue;
            }

            if (!inQuotes) {
                if (c == '[') {
                    // start of a new row
                    currentRow = new ArrayList<>();
                    token.setLength(0);
                    continue;
                }
                if (c == ']') {
                    // end of row -> flush last token into row
                    String v = token.toString().trim();
                    currentRow.add(v.isEmpty() ? null : v);
                    out.add(currentRow);
                    token.setLength(0);
                    continue;
                }
                if (c == ',') {
                    String v = token.toString().trim();
                    currentRow.add(v.isEmpty() ? null : v);
                    token.setLength(0);
                    continue;
                }
            }

            token.append(c);
        }

        return out;
    }

}
