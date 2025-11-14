package com.desapp.football_api.impl;

import com.desapp.football_api.exceptions.not_found.PlayerNotFoundException;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;
import com.desapp.football_api.model.team.Team;
import com.desapp.football_api.repository.PlayerRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.services.impl.PlayerServiceImpl;
import com.desapp.football_api.services.impl.StatsServiceImpl;
import com.desapp.football_api.services.impl.WhoScoredServiceImpl;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PlayerServiceImplMoreTest {

    @Mock
    WhoScoredServiceImpl whoScoredServiceImpl;
    @Mock
    PlayerRepository playerRepository;
    @Mock
    StatsServiceImpl statsServiceImpl;
    @Mock
    TeamRepository teamRepository;

    @InjectMocks
    PlayerServiceImpl playerServiceImpl;

    private String playerJson(long playerId, int teamId, String teamName) {
        // Minimal JSON for TablePlayerStats: list with one TableStat
        return "{\n" +
                "  \"playerTableStats\": [ {\n" +
                "    \"playerId\": " + playerId + ",\n" +
                "    \"name\": \"Leo Messi\",\n" +
                "    \"playedPositions\": \"FW\",\n" +
                "    \"age\": 36,\n" +
                "    \"regionCode\": \"AR\",\n" +
                "    \"teamId\": " + teamId + ",\n" +
                "    \"teamName\": \"" + teamName + "\"\n" +
                "  } ]\n" +
                "}";
    }

    @Test
    void getPlayerById_notFound_throws() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(PlayerNotFoundException.class, () -> playerServiceImpl.getPlayerById(99L));
    }

    @Test
    void getPlayerByIdAndType_hitInDb_returnsWithStats() {
        Player p = new Player();
        p.setId(7L);
        when(playerRepository.findById(7L)).thenReturn(Optional.of(p));
        PlayerStats stats = StatsType.Current.newInstance(new ArrayList<>());
        when(statsServiceImpl.getStatsByPlayerId(7L, StatsType.Current)).thenReturn(stats);
        p.setLastTimeScrapped(java.time.LocalDate.now());

        Player out = playerServiceImpl.getPlayerByIdAndType(7L, StatsType.Current);
        assertNotNull(out);
        assertSame(stats, out.getStats());
    }

    @Test
    void getPlayerByIdAndType_miss_triggersScrapeById() {
        when(playerRepository.findById(8L)).thenReturn(Optional.empty());
        // When scraping, it will fetch JSON and then save
        when(whoScoredServiceImpl.fetchJSONString(anyString())).thenReturn(playerJson(8L, 123, "PSG"));

        Team savedTeam = new Team(123L, "PSG", null, List.of(), List.of(), LocalDate.now());
        when(teamRepository.findById(123L)).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);

        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        Player out = playerServiceImpl.getPlayerByIdAndType(8L, StatsType.Current);
        assertNotNull(out);
        assertEquals(8L, out.getId());
        assertEquals(123L, out.getTeam().getId());
        assertEquals("PSG", out.getTeam().getName());
        assertEquals("Leo Messi", out.getFullname());
    }

    @Test
    void getPlayerByNameAndType_notFound_triggersScrapeByName() {
        when(playerRepository.findByFullname("Leo Messi")).thenReturn(Optional.empty());
        when(whoScoredServiceImpl.getIdFromFirstResult(eq("leo messi"), any())).thenReturn("50");
        when(whoScoredServiceImpl.fetchJSONString(anyString())).thenReturn(playerJson(50L, 321, "BAR"));
        when(teamRepository.findById(321L)).thenReturn(Optional.of(new Team(321L, "BAR", null, List.of(), List.of(), LocalDate.now())));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        Player out = playerServiceImpl.getPlayerByNameAndType("leo messi", StatsType.Current);
        assertNotNull(out);
        assertEquals(50L, out.getId());
    }

    @Test
    void scrapePlayerWithName_notFound_throws() {
        // whoScoredServiceImpl will execute the Runnable to throw a PlayerNotFoundException
        when(whoScoredServiceImpl.getIdFromFirstResult(eq("unknown"), any())).thenAnswer(inv -> {
            Runnable r = inv.getArgument(1);
            r.run();
            return null; // unreachable
        });
        assertThrows(PlayerNotFoundException.class, () -> playerServiceImpl.getPlayerByNameAndType("unknown", StatsType.Current));
    }

    @Test
    void scrapePlayerWithId_savesNewOrUpdatesExisting() {
        long playerId = 77L;
        String json = playerJson(playerId, 222, "Team Z");
        when(whoScoredServiceImpl.fetchJSONString(anyString())).thenReturn(json);
        // Case 1: player not present -> create new team if needed and save
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());
        when(teamRepository.findById(222L)).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        Player saved = playerServiceImpl.scrapePlayerWithIdAndType(playerId, StatsType.Current);
        assertNotNull(saved);
        assertEquals(playerId, saved.getId());
        assertEquals("Team Z", saved.getTeam().getName());

        // Case 2: existing player - with existing stats -> stats.setResume invoked
        Player existing = new Player();
        existing.setId(playerId);
        PlayerStats stats = StatsType.Current.newInstance(new ArrayList<>());
        existing.setStats(stats);
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(existing));
        when(statsServiceImpl.getStatsByPlayerId(playerId, StatsType.Current)).thenReturn(stats);

        Player updated = playerServiceImpl.scrapePlayerWithIdAndType(playerId, StatsType.Current);
        assertNotNull(updated);
        assertSame(stats, updated.getStats());

        // Case 3: existing player - without stats of type -> create and attach new stats
        when(statsServiceImpl.getStatsByPlayerId(playerId, StatsType.Historical)).thenReturn(null);
        Player updated2 = playerServiceImpl.scrapePlayerWithIdAndType(playerId, StatsType.Historical);
        assertNotNull(updated2.getStats());
    }

    @Test
    void getTableStat_playerDoesNotExist_throws() {
        // empty list in playerTableStats -> validatePlayerExists throws PlayerNotFoundException
        String emptyJson = "{\"playerTableStats\":[]}";
        when(whoScoredServiceImpl.fetchJSONString(anyString())).thenReturn(emptyJson);
        assertThrows(PlayerNotFoundException.class, () -> playerServiceImpl.scrapePlayerWithIdAndType(5L, StatsType.Current));
    }
}
