package com.desapp.football_api.impl;

import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.stats.player_stats.CurrentStats;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;
import com.desapp.football_api.repository.stats.PlayerStatsRepository;
import com.desapp.football_api.services.impl.StatsServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Mock
    PlayerStatsRepository playerStatsRepository;

    @InjectMocks
    StatsServiceImpl statsServiceImpl;

    @Test
    void getStatsByPlayerId_returnsExistingOrNull() {
        PlayerStats existing = new CurrentStats();
        when(playerStatsRepository.findByPlayerIdAndType(10L, StatsType.Current.getStatsClass()))
                .thenReturn(Optional.of(existing));
        when(playerStatsRepository.findByPlayerIdAndType(20L, StatsType.Current.getStatsClass()))
                .thenReturn(Optional.empty());

        assertSame(existing, statsServiceImpl.getStatsByPlayerId(10L, StatsType.Current));
        assertNull(statsServiceImpl.getStatsByPlayerId(20L, StatsType.Current));
    }

    @Test
    void saveOrUpdate_ignoresNulls_andSavesSettingExistingId() {
        // Null stats
        assertDoesNotThrow(() -> statsServiceImpl.saveOrUpdate(null, StatsType.Current));

        // Stats without player
        PlayerStats statsNoPlayer = new CurrentStats();
        assertDoesNotThrow(() -> statsServiceImpl.saveOrUpdate(statsNoPlayer, StatsType.Current));
        verify(playerStatsRepository, never()).save(any());

        // Stats with player but no id
        PlayerStats statsPlayerNoId = new CurrentStats();
        statsPlayerNoId.setPlayer(new Player());
        assertDoesNotThrow(() -> statsServiceImpl.saveOrUpdate(statsPlayerNoId, StatsType.Current));
        verify(playerStatsRepository, never()).save(any());

        // Existing stats present -> set ID then save
        Player p = new Player();
        p.setId(33L);
        PlayerStats stats = new CurrentStats();
        stats.setPlayer(p);

        PlayerStats existing = new CurrentStats();
        existing.setId(99L);
        when(playerStatsRepository.findByPlayerIdAndType(33L, StatsType.Current.getStatsClass()))
                .thenReturn(Optional.of(existing));

        when(playerStatsRepository.save(any(PlayerStats.class))).thenAnswer(inv -> inv.getArgument(0));

        statsServiceImpl.saveOrUpdate(stats, StatsType.Current);

        assertEquals(99L, stats.getId());
        verify(playerStatsRepository).save(eq(stats));
    }
}
