package com.desapp.football_api.config.enum_converter;

import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MatchLocationConverter implements Converter<String, MatchLocation> {
    @Override
    public MatchLocation convert(String source) {
        if (source == null || source.isBlank()) return MatchLocation.ALL;
        String s = source.trim().toLowerCase();
        switch (s) {
            case "all":
                return MatchLocation.ALL;
            case "home":
                return MatchLocation.HOME;
            case "away":
                return MatchLocation.AWAY;
            default:
                throw new IllegalArgumentException("Invalid MatchLocation: " + source + ". Allowed values: all, home, away");
        }
    }

}
