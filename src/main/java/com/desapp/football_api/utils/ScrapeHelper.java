package com.desapp.football_api.utils;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ScrapeHelper {
    private ScrapeHelper() {
    }

    public static <T> T getOrScrape(Supplier<T> fromDb, Predicate<T> condition, Supplier<T> fromScraping) {
        T entity = fromDb.get();
        if (condition.test(entity)) {
            entity = fromScraping.get();
        }
        return entity;
    }
}
