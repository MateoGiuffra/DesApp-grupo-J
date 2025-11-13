package com.desapp.football_api.config;

import com.desapp.football_api.config.enum_converter.MatchLocationConverter;
import com.desapp.football_api.config.enum_converter.MatchTypeConverter;
import com.desapp.football_api.config.enum_converter.StatsTypeConverter;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.model.player.StatsType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class EnumConvertersTest {

    @Test
    void statsTypeConverter_convertsValuesAndIsCaseInsensitive() {
        StatsTypeConverter c = new StatsTypeConverter();
        assertEquals(StatsType.Current, c.convert("current"));
        assertEquals(StatsType.Current, c.convert(" Current "));
        assertEquals(StatsType.Historical, c.convert("HISTORICAL"));
        assertEquals(StatsType.Current, c.convert(null));
        assertEquals(StatsType.Current, c.convert(" "));
        assertThrows(IllegalArgumentException.class, () -> c.convert("future"));
    }

    @Test
    void matchTypeConverter_convertsAndDefaultsToAll() {
        MatchTypeConverter c = new MatchTypeConverter();
        assertEquals(MatchType.ALL, c.convert("all"));
        assertEquals(MatchType.PAST, c.convert("past"));
        assertEquals(MatchType.UPCOMING, c.convert("upcoming"));
        assertEquals(MatchType.ALL, c.convert(null));
        assertEquals(MatchType.ALL, c.convert("   "));
        assertThrows(IllegalArgumentException.class, () -> c.convert("live"));
    }

    @Test
    void matchLocationConverter_convertsAndDefaultsToAll() {
        MatchLocationConverter c = new MatchLocationConverter();
        assertEquals(MatchLocation.ALL, c.convert("all"));
        assertEquals(MatchLocation.HOME, c.convert("home"));
        assertEquals(MatchLocation.AWAY, c.convert("AWAY"));
        assertEquals(MatchLocation.ALL, c.convert(null));
        assertEquals(MatchLocation.ALL, c.convert("   "));
        assertThrows(IllegalArgumentException.class, () -> c.convert("neutral"));
    }
}
