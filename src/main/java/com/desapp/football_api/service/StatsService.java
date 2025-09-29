package com.desapp.football_api.service;

import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.CurrentStats;
import com.desapp.football_api.model.stats.HistoricalStats;
import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.repository.CurrentStatsRepository;
import com.desapp.football_api.repository.HistoricalStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StatsService {

    @Autowired
    private HistoricalStatsRepository historicalStatsRepository;
    @Autowired
    private CurrentStatsRepository currentStatsRepository;

    @Transactional(readOnly = true)
    public Stats getStatsByPlayerId(Long id, StatsType type) {
        if (type == StatsType.Current) {
            return currentStatsRepository.findByPlayerId(id).orElse(null);
        }
        return historicalStatsRepository.findByPlayerId(id).orElse(null);
    }

    public void saveOrUpdate(Stats stats, StatsType type) {
        if (stats == null || stats.getPlayer() == null || stats.getPlayer().getId() == null) return;
        Long playerId = stats.getPlayer().getId();

        if (type == StatsType.Current) {
            currentStatsRepository.findByPlayerId(playerId)
                    .ifPresent(existing -> stats.setId(existing.getId()));
            currentStatsRepository.save((CurrentStats) stats);
        } else {
            historicalStatsRepository.findByPlayerId(playerId)
                    .ifPresent(existing -> stats.setId(existing.getId()));
            historicalStatsRepository.save((HistoricalStats) stats);
        }
    }
}