package com.desapp.football_api.service;

import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.player_stats.HistoricalStats;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;
import com.desapp.football_api.repository.stats.PlayerStatsRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    PlayerStatsRepository playerStatsRepository;

    @InjectMocks
    StatsService statsService;

    @Test
    void getStatsByPlayerId_missing_returnsNull() {
        when(playerStatsRepository.findByPlayerIdAndType(1L, HistoricalStats.class)).thenReturn(Optional.empty());
        assertNull(statsService.getStatsByPlayerId(1L, StatsType.Historical));
    }

    @Test
    void getStatsByPlayerId_found_returnsStats() {
        PlayerStats stats = new HistoricalStats(java.util.List.of());
        when(playerStatsRepository.findByPlayerIdAndType(2L, HistoricalStats.class)).thenReturn(Optional.of(stats));
        assertEquals(stats, statsService.getStatsByPlayerId(2L, StatsType.Historical));
    }

    @Test
    void saveOrUpdate_nulls_doNothing() {
        statsService.saveOrUpdate(null, StatsType.Historical);
        verifyNoInteractions(playerStatsRepository);
    }

    @Test
    void saveOrUpdate_updatesExistingIdAndSaves() {
        HistoricalStats stats = new HistoricalStats(java.util.List.of());
        Player player = new Player();
        player.setId(10L);
        stats.setPlayer(player);

        HistoricalStats existing = new HistoricalStats(java.util.List.of());
        existing.setId(99L);

        when(playerStatsRepository.findByPlayerIdAndType(10L, HistoricalStats.class)).thenReturn(Optional.of(existing));
        when(playerStatsRepository.save(any(HistoricalStats.class))).thenAnswer(inv -> inv.getArgument(0));

        statsService.saveOrUpdate(stats, StatsType.Historical);

        assertEquals(99L, stats.getId());
        verify(playerStatsRepository).save(any(HistoricalStats.class));
    }
}
