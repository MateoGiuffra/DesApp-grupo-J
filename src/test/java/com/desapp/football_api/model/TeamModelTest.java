package com.desapp.football_api.model;

import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.model.player.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class TeamModelTest {

    private static String fmt(LocalDate d) {
        return d.format(DateTimeFormatter.ofPattern("dd-MM-yy"));
    }

    private static Match m(long id, LocalDate date, String time, long homeId, String home, long awayId, String away, String comp, Team team) {
        return new Match(id, fmt(date), time, homeId, home, awayId, away, comp, team);
    }

    @Test
    void addAndRemovePlayer_keepBidirectionalConsistency() {
        Team t = new Team(10L, "Team X", List.of());
        Player p = new Player();
        p.setId(7L);

        t.addPlayer(p);
        assertEquals(1, t.getSquadList().size());
        assertEquals(t, p.getTeam());

        // adding same player again should not duplicate
        t.addPlayer(p);
        assertEquals(1, t.getSquadList().size());

        t.removePlayer(p);
        assertTrue(t.getSquadList().isEmpty());
        assertNull(p.getTeam());

        // null-safe
        t.addPlayer(null);
        t.removePlayer(null);
        assertTrue(t.getSquadList().isEmpty());
    }

    @Test
    void matchFilters_past_upcoming_home_away_and_intersection() {
        Team t = new Team(65L, "Barcelona", List.of());
        LocalDate today = LocalDate.now();

        Match pastHome = m(1, today.minusDays(2), "18:00", 65, "Barca", 10, "Rayo", "LaLiga", t);
        Match futureHome = m(2, today.plusDays(3), "18:00", 65, "Barca", 20, "Betis", "LaLiga", t);
        Match futureAway = m(3, today.plusDays(5), "20:00", 30, "Sevilla", 65, "Barca", "LaLiga", t);
        Match pastAway = m(4, today.minusDays(1), "21:00", 40, "Valencia", 65, "Barca", "LaLiga", t);

        t.applyMatches(List.of(pastHome, futureHome, futureAway, pastAway));

        assertEquals(2, t.getPastMatches().size());
        assertEquals(2, t.getUpcomingMatches().size());
        assertEquals(2, t.getHomeMatches().size());
        assertEquals(2, t.getAwayMatches().size());

        // Intersection: UPCOMING and HOME should be only futureHome
        List<Match> upcomingHome = t.getFilterMatches(MatchType.UPCOMING, MatchLocation.HOME);
        assertEquals(1, upcomingHome.size());
        assertEquals(2L, upcomingHome.getFirst().getId());

        // ALL x AWAY should be both away matches
        List<Match> allAway = t.getFilterMatches(MatchType.ALL, MatchLocation.AWAY);
        assertEquals(2, allAway.size());
    }

    @Test
    void applyAggregates_replaceAndWireRelations() {
        Team t = new Team(1L, "X", List.of());
        Player p1 = new Player(); p1.setId(1L);
        Player p2 = new Player(); p2.setId(2L);
        t.applyPlayers(List.of(p1, p2));

        assertEquals(2, t.getSquadList().size());
        assertEquals(t, p1.getTeam());
        assertEquals(t, p2.getTeam());

        // Replace players
        Player p3 = new Player(); p3.setId(3L);
        t.applyPlayers(List.of(p3));
        assertEquals(1, t.getSquadList().size());
        assertEquals(t, p3.getTeam());

        // applyMatches wires team in each match and replaces list
        Match m1 = new Match(); m1.setId(11L);
        Match m2 = new Match(); m2.setId(12L);
        t.applyMatches(List.of(m1, m2));
        assertEquals(2, t.getMatches().size());
        assertEquals(t, t.getMatches().get(0).getTeam());
        assertEquals(t, t.getMatches().get(1).getTeam());

        // null list -> becomes empty
        t.applyMatches(null);
        assertNotNull(t.getMatches());
        assertEquals(0, t.getMatches().size());
    }
}
