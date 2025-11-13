package com.desapp.football_api.model;

import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.team.Team;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Tag("unit")
class MatchLocationTest {

    @Test
    void isAtHome_valuesForEachEnum() {
        assertEquals(null, MatchLocation.ALL.isAtHome());
        assertEquals(Boolean.TRUE, MatchLocation.HOME.isAtHome());
        assertEquals(Boolean.FALSE, MatchLocation.AWAY.isAtHome());
    }

    @Test
    void filter_delegatesToTeamMethods() {
        Team team = mock(Team.class);
        List<Match> all = List.of(mock(Match.class));
        List<Match> home = List.of(mock(Match.class));
        List<Match> away = List.of(mock(Match.class));

        when(team.getMatches()).thenReturn(all);
        when(team.getHomeMatches()).thenReturn(home);
        when(team.getAwayMatches()).thenReturn(away);

        assertEquals(all, MatchLocation.ALL.filter(team));
        assertEquals(home, MatchLocation.HOME.filter(team));
        assertEquals(away, MatchLocation.AWAY.filter(team));

        verify(team, times(1)).getMatches();
        verify(team, times(1)).getHomeMatches();
        verify(team, times(1)).getAwayMatches();
        verifyNoMoreInteractions(team);
    }
}
