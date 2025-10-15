package com.desapp.football_api.services;

import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.TeamStats;
import com.desapp.football_api.model.table_stats.TableStat;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.repository.stats.PlayerStatsRepository;
import com.desapp.football_api.service.PlayerService;
import com.desapp.football_api.service.TeamService;
import com.desapp.football_api.service.WhoScoredService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TeamServiceUnitTest {

    @Mock
    WhoScoredService whoScoredService;
    @Mock
    PlayerService playerService;
    @Mock
    TeamRepository teamRepository;
    @Mock
    PlayerStatsRepository playerStatsRepository;

    @InjectMocks
    TeamService teamService;

    @Test
    void hasToAddPlayersStatsAndMatchesToTeam_variousConditions() {
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

        // player with correct stats type but missing team stats and matches -> still true
        Player pOk = new Player(3L, "P", null, null, null, new ArrayList<TableStat>(), StatsType.Historical, null);
        Team tIncomplete = new Team(1L, "X", List.of(pOk));
        assertTrue(teamService.hasToScrap(tIncomplete, StatsType.Current));

        // complete team: stats present and at least one match -> false
        Team tOk = new Team(1L, "X", List.of(pOk));
        tOk.setStats(new TeamStats(List.of()));
        Match m = new Match();
        m.setId(99L);
        tOk.setMatches(List.of(m));
        assertFalse(teamService.hasToScrap(tOk, StatsType.Current));
    }

    @Test
    void scrapeTeamByIdAndType_fetchesPlayersAndSavesTeam() {
        String teamBody = "{" +
                "\"teamName\":\"River\"," +
                "\"playerTableStats\":[{" +
                "\"playerId\":11},{\"playerId\":22}]}";
        String teamStatsBody = "{\"teamTableStats\":[{\"possession\":0.5,\"passSuccess\":0.8,\"shotsPerGame\":10}]}";
        String fixturesBody = "[]"; // empty fixtures payload is acceptable

        when(whoScoredService.fetchJSONString(any(String.class)))
                .thenAnswer(inv -> {
                    String url = inv.getArgument(0);
                    if (url == null) return teamBody;
                    if (url.contains("getplayerstatistics") && url.contains("teamIds=")) return teamBody;
                    if (url.contains("getteamstatistics")) return teamStatsBody;
                    if (url.contains("teamsfeed") && url.contains("fixtures")) return fixturesBody;
                    return teamBody;
                });

        Player p1 = new Player(11L, "A", null, null, null, List.of(), StatsType.Current, null);
        Player p2 = new Player(22L, "B", null, null, null, List.of(), StatsType.Current, null);
        when(playerService.createPlayer(eq(11L), eq(StatsType.Current), any(Team.class))).thenReturn(p1);
        when(playerService.createPlayer(eq(22L), eq(StatsType.Current), any(Team.class))).thenReturn(p2);

        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        Team result = teamService.scrapeTeamByIdAndType(100L, StatsType.Current);
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("River", result.getName());
        assertEquals(2, result.getSquadList().size());

        verify(teamRepository).save(any(Team.class));
        verify(playerService).createPlayer(eq(11L), eq(StatsType.Current), any(Team.class));
        verify(playerService).createPlayer(eq(22L), eq(StatsType.Current), any(Team.class));
    }

    @Test
    void getTeamByName_returnsTeamByType() {
        Player p = new Player();
        p.setId(7L);
        Team team = new Team(1L, "T", List.of(p));

        when(teamRepository.findByNameAndSquadType(any(String.class), any())).thenReturn(Optional.of(team));

        Team out = teamService.getTeamByName("Name", StatsType.Current);
        assertNotNull(out);
        assertEquals(1, out.getSquadList().size());
        verify(playerStatsRepository, never()).findByPlayerIdAndType(any(), any());
    }
}
