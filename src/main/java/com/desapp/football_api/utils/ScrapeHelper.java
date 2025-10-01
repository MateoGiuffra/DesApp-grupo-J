package com.desapp.football_api.utils;

import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScrapeHelper {
    public static <T> T getOrScrape(Supplier<T> fromDb, Function<T, Boolean> condition, Supplier<T> fromScraping) throws IOException, InterruptedException {
        T entity = fromDb.get();
        if (condition.apply(entity)) {
            entity = fromScraping.get();
        }
        return entity;
    }
}
