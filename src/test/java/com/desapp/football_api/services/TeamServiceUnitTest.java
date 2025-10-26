package com.desapp.football_api.services;

import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.TeamStats;
import com.desapp.football_api.model.stats.player_stats.HistoricalStats;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.service.PlayerService;
import com.desapp.football_api.service.TeamService;
import com.desapp.football_api.service.WhoScoredService;
import com.desapp.football_api.utils.WhoScoredLink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TeamServiceUnitTest {

    @Mock
    private WhoScoredService whoScoredService;
    @Mock
    private PlayerService playerService;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private TeamService teamService;

    private Team baseTeam;
    private Player basePlayer;

    @BeforeEach
    void setUp() {
        basePlayer = new Player(1L, "Player", "pos", "dob", "nat", new ArrayList<>(), StatsType.Historical, null,
                LocalDate.now());
        baseTeam = new Team(1L, "Team", new TeamStats(), List.of(basePlayer), List.of(new Match()));
        baseTeam.setLastTimeScrapped(LocalDate.now());
    }

    @Test
    void hasToScrap_variousConditions() {
        assertTrue(teamService.hasToScrap(null, StatsType.Current));

        Team team = new Team(1L, "Test", null, new ArrayList<>(), new ArrayList<>());
        assertTrue(teamService.hasToScrap(team, StatsType.Current), "Should scrap if squad list is empty");

        team.setSquadList(List.of(new Player()));
        assertTrue(teamService.hasToScrap(team, StatsType.Current), "Should scrap if matches are empty");

        team.setMatches(List.of(new Match()));
        assertTrue(teamService.hasToScrap(team, StatsType.Current), "Should scrap if stats are null");

        team.setStats(new TeamStats());
        assertTrue(teamService.hasToScrap(team, StatsType.Current), "Should scrap if a player has null stats");

        Player playerWithStats = new Player();
        playerWithStats.setStats(new HistoricalStats());
        team.setSquadList(List.of(playerWithStats));
        assertTrue(teamService.hasToScrap(team, StatsType.Current), "Should scrap if player stats type is different");

        team.setLastTimeScrapped(LocalDate.now().minusDays(5));
        assertTrue(teamService.hasToScrap(team, StatsType.Current), "Should scrap if outdated");

        team.setLastTimeScrapped(LocalDate.now());
        playerWithStats.setStats(StatsType.Current.newInstance(new ArrayList<>()));
        assertFalse(teamService.hasToScrap(team, StatsType.Current), "Should not scrap if everything is up to date");
    }


    @Test
    void scrapeTeamByIdAndType_fetchesAndSavesTeam() throws Exception {
        Long teamId = 100L;
        StatsType type = StatsType.Current;
        String teamName = "River";

        String teamBody = "{\"teamName\":\"" + teamName + "\",\"playerTableStats\":[{\"playerId\":11}," +
                "{\"playerId\":22}]}";
        String teamStatsBody = "{\"teamTableStats\":[{\"field\":\"Team\",\"stat\":{\"possession\":60.5}}]}";
        String fixturesBody = "[{\"fixtureId\": 1}]";

        lenient().when(whoScoredService.fetchJSONString(WhoScoredLink.getTeamLink(teamId))).thenReturn(teamBody);
        lenient().when(whoScoredService.fetchJSONString(WhoScoredLink.getTeamStatsLink(teamId))).thenReturn(teamStatsBody);
        lenient().when(whoScoredService.fetchJSONString(WhoScoredLink.getTeamFixturesLink(teamId))).thenReturn(fixturesBody);

        Player p1 = new Player();
        p1.setId(11L);
        Player p2 = new Player();
        p2.setId(22L);
        when(playerService.createPlayer(eq(11L), eq(type), any(Team.class))).thenReturn(p1);
        when(playerService.createPlayer(eq(22L), eq(type), any(Team.class))).thenReturn(p2);

        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        Team result = teamService.scrapeTeamByIdAndType(teamId, type);

        assertNotNull(result);
        assertEquals(teamId, result.getId());
        assertEquals(teamName, result.getName());
        assertEquals(2, result.getSquadList().size());
        assertNotNull(result.getStats());
        assertNotNull(result.getMatches());
        assertTrue(result.getMatches().isEmpty());
    }

    @Test
    void getTeamById_returnsTeam() {
        Long teamId = 1L;
        when(teamRepository.findByIdAndSquadType(any(Long.class), any())).thenReturn(Optional.of(baseTeam));

        Team result = teamService.getTeamById(teamId, StatsType.Historical);

        assertNotNull(result);
        assertEquals(baseTeam.getId(), result.getId());
    }
}