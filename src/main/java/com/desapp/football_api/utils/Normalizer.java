package com.desapp.football_api.utils;

import java.util.Arrays;

public class Normalizer {
    public static String normalizeName(String name) {
        if (name == null || name.isBlank()) return name;

        return Arrays.stream(name.trim().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(name);
    }
}
