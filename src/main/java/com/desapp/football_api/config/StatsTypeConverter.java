package com.desapp.football_api.config;

import com.desapp.football_api.model.player.StatsType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StatsTypeConverter implements Converter<String, StatsType> {
    @Override
    public StatsType convert(String source) {
        if (source == null || source.isBlank()) return StatsType.Current;
        String s = source.trim().toLowerCase();
        switch (s) {
            case "current":
                return StatsType.Current;
            case "historical":
                return StatsType.Historical;
            default:
                throw new IllegalArgumentException("Invalid StatsType: " + source + ". Allowed values: current, historical");
        }
    }
}
