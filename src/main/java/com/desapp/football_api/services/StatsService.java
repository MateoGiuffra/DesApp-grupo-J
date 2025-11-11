package com.desapp.football_api.services;

import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;

public interface StatsService {
    PlayerStats getStatsByPlayerId(Long id, StatsType type);

    void saveOrUpdate(PlayerStats stats, StatsType type);
}
