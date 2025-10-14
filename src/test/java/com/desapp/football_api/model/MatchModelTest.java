package com.desapp.football_api.model;

import com.desapp.football_api.model.match.Match;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class MatchModelTest {

    @Test
    void constructor_parsesDateWithTwoDigitYear() {
        Team t = new Team(1L, "T", null);
        Match m = new Match(100L, "01-12-25", "18:00", 10L, "Home", 20L, "Away ", "League", t);
        assertEquals(LocalDate.of(2025, 12, 1), m.getDate());
        assertEquals("Away", m.getAwayTeamName()); // trims
        assertEquals(t, m.getTeam());
    }

    @Test
    void constructor_parsesDateWithFourDigitYear() {
        Team t = new Team(2L, "T2", null);
        Match m = new Match(101L, "02-01-2026", "21:00", 11L, "Home2", 21L, "Away2", "Cup", t);
        assertEquals(LocalDate.of(2026, 1, 2), m.getDate());
    }
}
