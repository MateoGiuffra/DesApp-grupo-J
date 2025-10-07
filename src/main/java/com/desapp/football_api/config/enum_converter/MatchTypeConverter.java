package com.desapp.football_api.config.enum_converter;

import com.desapp.football_api.model.match.MatchType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class MatchTypeConverter implements Converter<String, MatchType> {
    @Override
    public MatchType convert(String source) {
        if (source == null || source.isBlank()) return MatchType.ALL;
        String s = source.trim().toLowerCase();
        switch (s) {
            case "all":
                return MatchType.ALL;
            case "past":
                return MatchType.PAST;
            case "upcoming":
                return MatchType.UPCOMING;
            default:
                throw new IllegalArgumentException("Invalid MatchType: " + source + ". Allowed values: all, past, upcoming");
        }
    }
}
