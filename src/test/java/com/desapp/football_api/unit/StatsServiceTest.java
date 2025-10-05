package com.desapp.football_api.unit;

import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.stats.HistoricalStats;
import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.repository.StatsRepository;
import com.desapp.football_api.service.StatsService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    StatsRepository statsRepository;

    @InjectMocks
    StatsService statsService;

    @Test
    void getStatsByPlayerId_missing_returnsNull() {
        when(statsRepository.findByPlayerIdAndType(1L, HistoricalStats.class)).thenReturn(Optional.empty());
        assertNull(statsService.getStatsByPlayerId(1L, StatsType.Historical));
    }

    @Test
    void getStatsByPlayerId_found_returnsStats() {
        Stats stats = new HistoricalStats(java.util.List.of());
        when(statsRepository.findByPlayerIdAndType(2L, HistoricalStats.class)).thenReturn(Optional.of(stats));
        assertEquals(stats, statsService.getStatsByPlayerId(2L, StatsType.Historical));
    }

    @Test
    void saveOrUpdate_nulls_doNothing() {
        statsService.saveOrUpdate(null, StatsType.Historical);
        verifyNoInteractions(statsRepository);
    }

    @Test
    void saveOrUpdate_updatesExistingIdAndSaves() {
        HistoricalStats stats = new HistoricalStats(java.util.List.of());
        Player player = new Player();
        player.setId(10L);
        stats.setPlayer(player);

        HistoricalStats existing = new HistoricalStats(java.util.List.of());
        existing.setId(99L);

        when(statsRepository.findByPlayerIdAndType(10L, HistoricalStats.class)).thenReturn(Optional.of(existing));
        when(statsRepository.save(any(HistoricalStats.class))).thenAnswer(inv -> inv.getArgument(0));

        statsService.saveOrUpdate(stats, StatsType.Historical);

        assertEquals(99L, stats.getId());
        verify(statsRepository).save(any(HistoricalStats.class));
    }
}
