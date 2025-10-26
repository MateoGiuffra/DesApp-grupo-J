package com.desapp.football_api.utils;

import java.util.function.Function;
import java.util.function.Supplier;

public class ScrapeHelper {
    private ScrapeHelper() {
    }

    public static <T> T getOrScrape(Supplier<T> fromDb, Function<T, Boolean> condition, Supplier<T> fromScraping) {
        T entity = fromDb.get();
        if (Boolean.TRUE.equals(condition.apply(entity))) {
            entity = fromScraping.get();
        }
        return entity;
    }
}
