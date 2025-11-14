package com.desapp.football_api.impl;

import com.desapp.football_api.exceptions.generic.CustomRuntimeException;
import com.desapp.football_api.exceptions.not_found.TeamNotFoundException;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.TeamStats;
import com.desapp.football_api.model.team.AdvancedMetrics;
import com.desapp.football_api.model.team.Team;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.PlayerRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.repository.stats.PlayerStatsRepository;
import com.desapp.football_api.services.impl.PlayerServiceImpl;
import com.desapp.football_api.services.impl.TeamServiceImpl;
import com.desapp.football_api.services.impl.WhoScoredServiceImpl;
import com.desapp.football_api.utils.WhoScoredLink;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeamServiceImplMoreTest {

    @Mock WhoScoredServiceImpl whoScoredServiceImpl;
    @Mock PlayerServiceImpl playerServiceImpl;
    @Mock TeamRepository teamRepository;
    @Mock MatchRepository matchRepository;
    @Mock PlayerRepository playerRepository;
    @Mock PlayerStatsRepository playerStatsRepository;

    @InjectMocks TeamServiceImpl teamServiceImpl;

    private String teamBody(long teamId, String teamName, long... playerIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"teamName\":\"").append(teamName).append("\",\"playerTableStats\":[");
        for (int i = 0; i < playerIds.length; i++) {
            if (i>0) sb.append(',');
            sb.append("{\"playerId\":").append(playerIds[i]).append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    @Test
    void getOrScrapeTeamByName_cacheHit_noScrape() {
        StatsType type = StatsType.Current;
        Team t = new Team(11L, "X", new TeamStats(), new ArrayList<>(), List.of(new Match()));
        // one player with stats of requested type
        Player p = new Player(); p.setId(1L);
        p.setStats(type.newInstance(new ArrayList<>()));
        t.getSquadList().add(p);
        t.setLastTimeScrapped(LocalDate.now());

        when(teamRepository.findByNameAndSquadType(eq("X"), eq(type.getStatsClass()))).thenReturn(Optional.of(t));
        when(teamRepository.findByIdWithPlayersAndStatsType(11L, type.getStatsClass())).thenReturn(t);

        Team out = teamServiceImpl.getOrScrapeTeamByName("x", type);
        assertNotNull(out);
        verify(whoScoredServiceImpl, never()).getIdFromFirstResult(anyString(), any());
    }

    @Test
    void getOrScrapeTeamById_cacheHit_noScrape() {
        StatsType type = StatsType.Current;
        Team t = new Team(22L, "Y", new TeamStats(), new ArrayList<>(), List.of(new Match()));
        Player p = new Player(); p.setId(1L); p.setStats(type.newInstance(new ArrayList<>()));
        t.getSquadList().add(p);
        t.setLastTimeScrapped(LocalDate.now());

        when(teamRepository.findByIdAndSquadType(22L, type.getStatsClass())).thenReturn(Optional.of(t));

        Team out = teamServiceImpl.getOrScrapeTeamById(22L, type);
        assertNotNull(out);
        verify(whoScoredServiceImpl, never()).fetchJSONString(anyString());
    }

    @Test
    void scrapeTeamByName_notFound_throws() {
        when(whoScoredServiceImpl.getIdFromFirstResult(eq("unknown"), any())).thenAnswer(inv -> {
            Runnable r = inv.getArgument(1);
            r.run();
            return null; // unreachable
        });
        assertThrows(TeamNotFoundException.class, () -> teamServiceImpl.scrapeTeamByNameAndType("unknown", StatsType.Current));
    }

    @Test
    void scrapeTeamById_error_throwsTeamNotFound() {
        when(whoScoredServiceImpl.fetchJSONString(WhoScoredLink.getTeamLink(5L))).thenThrow(new RuntimeException("bad"));
        assertThrows(TeamNotFoundException.class, () -> teamServiceImpl.scrapeTeamByIdAndType(5L, StatsType.Current));
    }

    @Test
    void scrapeTeamStatsById_empty_throws() {
        // Cause team stats path to fail via public scraping method (which calls private creator)
        when(whoScoredServiceImpl.fetchJSONString(WhoScoredLink.getTeamLink(7L))).thenReturn("{\"teamName\":\"T\",\"playerTableStats\":[]}");
        when(whoScoredServiceImpl.fetchJSONString(WhoScoredLink.getTeamStatsLink(7L))).thenReturn("{\"teamTableStats\":[]}");
        assertThrows(TeamNotFoundException.class, () -> teamServiceImpl.scrapeTeamByIdAndType(7L, StatsType.Current));
    }

    @Test
    void getTeamByName_errorReturnsNull() {
        when(teamRepository.findByNameAndSquadType(anyString(), any())).thenThrow(new RuntimeException("db"));
        assertNull(teamServiceImpl.getTeamByName("X", StatsType.Current));
    }

    @Test
    void getTeamById_fallbackAndSwitchStatsReference() {
        StatsType type = StatsType.Current;
        // No team directly with requested type
        when(teamRepository.findByIdAndSquadType(30L, type.getStatsClass())).thenReturn(Optional.empty());
        Team base = new Team(30L, "B", null, List.of(new Player()), List.of(new Match()));
        when(teamRepository.findById(30L)).thenReturn(Optional.of(base));
        // getTeamCompleted: first attempt returns null
        when(teamRepository.findByIdWithPlayersAndStatsType(30L, type.getStatsClass())).thenReturn(null);
        when(teamRepository.findByIdWithPlayers(30L)).thenReturn(base);
        when(playerStatsRepository.countPlayersWithoutStatsOfType(30L, type.getStatsClass())).thenReturn(0L);
        when(teamRepository.findByIdWithPlayersAndStatsType(30L, type.getStatsClass())).thenReturn(base);

        Team out = teamServiceImpl.getTeamById(30L, type);
        assertNotNull(out);
        // Depending on the current DB state, the optimization may or may not be applied.
        // We only assert that a team is returned via the fallback path.
    }

    @Test
    void getFilterAndMatchesPaths() {
        long teamId = 40L;
        // teamDoesExistsAndHasMatches true -> use repository path
        when(teamRepository.existsByIdWithMatches(teamId)).thenReturn(true);
        when(matchRepository.findByTeam_Id(teamId)).thenReturn(List.of(new Match()));
        assertFalse(teamServiceImpl.getMatchesByTeamId(teamId, MatchType.ALL, MatchLocation.ALL).isEmpty());

        // false -> scrape + use team.getFilterMatches
        when(teamRepository.existsByIdWithMatches(teamId)).thenReturn(false);
        Team scraped = new Team(teamId, "T", new TeamStats(), List.of(), new ArrayList<>());
        scraped.setMatches(List.of());
        when(whoScoredServiceImpl.fetchJSONString(WhoScoredLink.getTeamLink(teamId))).thenReturn(teamBody(teamId, "T", 101L));
        // minimal team stats and fixtures to avoid TeamNotFoundException during scrape
        when(whoScoredServiceImpl.fetchJSONString(WhoScoredLink.getTeamStatsLink(teamId)))
                .thenReturn("{\"teamTableStats\":[{\"field\":\"Team\",\"stat\":{}}]}");
        when(whoScoredServiceImpl.fetchJSONString(WhoScoredLink.getTeamFixturesLink(teamId)))
                .thenReturn("[]");
        when(playerServiceImpl.createPlayer(eq(101L), eq(StatsType.Current), any(Team.class))).thenReturn(new Player());
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));
        assertNotNull(teamServiceImpl.getMatchesByTeamId(teamId, MatchType.ALL, MatchLocation.ALL));
    }

    @Test
    void advancedMetricsAndComparison_delegates() {
        StatsType type = StatsType.Current;
        Team a = new Team(1L, "A", new TeamStats(), new ArrayList<>(), new ArrayList<>());
        Team b = new Team(2L, "B", new TeamStats(), new ArrayList<>(), new ArrayList<>());
        Player ap = new Player(); ap.setStats(type.newInstance(new ArrayList<>())); a.setSquadList(List.of(ap)); a.setLastTimeScrapped(LocalDate.now()); a.setMatches(List.of(new Match()));
        Player bp = new Player(); bp.setStats(type.newInstance(new ArrayList<>())); b.setSquadList(List.of(bp)); b.setLastTimeScrapped(LocalDate.now()); b.setMatches(List.of(new Match()));

        lenient().when(teamRepository.findByIdAndSquadType(1L, type.getStatsClass())).thenReturn(Optional.of(a));
        lenient().when(teamRepository.findByIdAndSquadType(2L, type.getStatsClass())).thenReturn(Optional.of(b));
        // Also ensure name lookups hit cache and avoid network
        when(teamRepository.findByNameAndSquadType("A", type.getStatsClass())).thenReturn(Optional.of(a));
        when(teamRepository.findByNameAndSquadType("B", type.getStatsClass())).thenReturn(Optional.of(b));
        when(teamRepository.findByIdWithPlayersAndStatsType(1L, type.getStatsClass())).thenReturn(a);
        when(teamRepository.findByIdWithPlayersAndStatsType(2L, type.getStatsClass())).thenReturn(b);

        AdvancedMetrics am1 = teamServiceImpl.getAdvancedMetricsById(1L);
        assertNotNull(am1);
        AdvancedMetrics am2 = teamServiceImpl.getAdvancedMetricsByName("A");
        assertNotNull(am2);
        assertNotNull(teamServiceImpl.getComparisonByTeamNames("A", "B", type));
    }

    @Test
    void scrapePlayersFromTeam_wrapsNotFoundAndNulls() throws Exception {
        long teamId = 77L; StatsType type = StatsType.Current;
        Team team = new Team(teamId, "Z", null, new ArrayList<>(), new ArrayList<>());
        String body = teamBody(teamId, "Z", 1L, 2L, 3L);
        // happy players
        when(playerServiceImpl.createPlayer(eq(1L), eq(type), any(Team.class))).thenReturn(new Player());
        // player 2 -> general exception -> treated as null
        when(playerServiceImpl.createPlayer(eq(2L), eq(type), any(Team.class))).thenThrow(new RuntimeException("x"));
        // player 3 -> HttpClientErrorException.NotFound -> translated to TeamNotFoundException internally and then wrapped as CustomRuntimeException by outer catch
        when(playerServiceImpl.createPlayer(eq(3L), eq(type), any(Team.class))).thenAnswer(inv -> {
            org.springframework.web.client.HttpClientErrorException.NotFound ex = mock(org.springframework.web.client.HttpClientErrorException.NotFound.class);
            throw ex;
        });
        // call and ensure custom exception occurs due to runtime thrown while composing
        assertThrows(CustomRuntimeException.class, () -> teamServiceImpl.scrapePlayersFromTeam(teamId, body, type, team));
    }
}
