package com.desapp.football_api.unit;

import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.repository.stats.PlayerStatsRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.service.PlayerService;
import com.desapp.football_api.service.TeamService;
import com.desapp.football_api.service.WhoScoredService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TeamServiceUnitTest {

    @Mock WhoScoredService whoScoredService;
    @Mock PlayerService playerService;
    @Mock TeamRepository teamRepository;
    @Mock
    PlayerStatsRepository playerStatsRepository;

    @InjectMocks TeamService teamService;

    @Test
    void hasToScrap_variousConditions() {
        // null team
        assertTrue(teamService.hasToScrap(null, StatsType.Current));

        // empty squad
        Team tEmpty = new Team(1L, "X", List.of());
        assertTrue(teamService.hasToScrap(tEmpty, StatsType.Current));

        // player without stats
        Player pNoStats = new Player();
        pNoStats.setId(1L);
        Team tNoStats = new Team(1L, "X", List.of(pNoStats));
        assertTrue(teamService.hasToScrap(tNoStats, StatsType.Current));

        // player with different stats type
        Player pWrongStats = new Player(2L, "P", null, null, null, List.of(), StatsType.Historical, null);
        Team tWrong = new Team(1L, "X", List.of(pWrongStats));
        assertTrue(teamService.hasToScrap(tWrong, StatsType.Current));

        // player with correct stats type
        Player pOk = new Player(3L, "P", null, null, null, List.of(), StatsType.Current, null);
        Team tOk = new Team(1L, "X", List.of(pOk));
        assertFalse(teamService.hasToScrap(tOk, StatsType.Current));
    }

    @Test
    void scrapeTeamByIdAndType_fetchesPlayersAndSavesTeam() {
        String body = "{" +
                "\"teamName\":\"River\"," +
                "\"playerTableStats\":[{" +
                "\"playerId\":11},{\"playerId\":22}]}";

        when(whoScoredService.fetchJSONString(any())).thenReturn(body);

        Player p1 = new Player(11L, "A", null, null, null, List.of(), StatsType.Current, null);
        Player p2 = new Player(22L, "B", null, null, null, List.of(), StatsType.Current, null);
        when(playerService.scrapePlayerWithIdAndType(11L, StatsType.Current)).thenReturn(p1);
        when(playerService.scrapePlayerWithIdAndType(22L, StatsType.Current)).thenReturn(p2);

        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        Team result = teamService.scrapeTeamByIdAndType(100L, StatsType.Current);
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("River", result.getName());
        assertEquals(2, result.getSquadList().size());

        verify(teamRepository).save(any(Team.class));
        verify(playerService).scrapePlayerWithIdAndType(11L, StatsType.Current);
        verify(playerService).scrapePlayerWithIdAndType(22L, StatsType.Current);
    }

    @Test
    void getTeamWithPlayers_populatesStatsIfPresent() {
        Player p = new Player();
        p.setId(7L);
        Team team = new Team(1L, "T", List.of(p));

        when(teamRepository.findByName("Name")).thenReturn(Optional.of(team));
        when(playerStatsRepository.findByPlayerIdAndType(eq(7L), any())).thenReturn(Optional.empty());

        Team out = teamService.getTeamByName("Name", StatsType.Current);
        assertNotNull(out);
        assertEquals(1, out.getSquadList().size());
        verify(playerStatsRepository).findByPlayerIdAndType(eq(7L), any());
    }
}
