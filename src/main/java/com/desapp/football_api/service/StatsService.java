package com.desapp.football_api.service;

import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.HistoricalStats;
import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.repository.StatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class StatsService {

    @Autowired
    private StatsRepository statsRepository;

    @Transactional(readOnly = true)
    public Stats getStatsByPlayerId(Long id, StatsType type) {
        return statsRepository.findByPlayerIdAndType(id, type.getStatsClass()).orElse(null);
    }

    public void saveOrUpdate(Stats stats, StatsType type) {
        if (stats == null || stats.getPlayer() == null || stats.getPlayer().getId() == null) return;
        Long playerId = stats.getPlayer().getId();
        statsRepository.findByPlayerIdAndType(playerId, type.getStatsClass()).ifPresent(existing -> stats.setId(existing.getId()));
        statsRepository.save((HistoricalStats) stats);
    }
}