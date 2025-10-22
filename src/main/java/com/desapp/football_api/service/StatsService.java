package com.desapp.football_api.service;

import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.player_stats.HistoricalStats;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;
import com.desapp.football_api.repository.stats.PlayerStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StatsService {
    private final PlayerStatsRepository playerStatsRepository;

    public StatsService(PlayerStatsRepository playerStatsRepository) {
        this.playerStatsRepository = playerStatsRepository;
    }

    @Transactional(readOnly = true)
    public PlayerStats getStatsByPlayerId(Long id, StatsType type) {
        return playerStatsRepository.findByPlayerIdAndType(id, type.getStatsClass()).orElse(null);
    }

    public void saveOrUpdate(PlayerStats stats, StatsType type) {
        if (stats == null || stats.getPlayer() == null || stats.getPlayer().getId() == null) return;
        Long playerId = stats.getPlayer().getId();
        playerStatsRepository.findByPlayerIdAndType(playerId, type.getStatsClass()).ifPresent(existing -> stats.setId(existing.getId()));
        playerStatsRepository.save((HistoricalStats) stats);
    }
}