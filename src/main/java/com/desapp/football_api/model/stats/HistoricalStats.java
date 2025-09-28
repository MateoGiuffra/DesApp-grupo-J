package com.desapp.football_api.model.stats;

import com.desapp.football_api.model.table_player_stats.PlayerTableStat;

import java.util.List;

public class HistoricalStats extends Stats {
    public HistoricalStats(List<PlayerTableStat> playerTableStats) {
        super(playerTableStats);
    }

    public HistoricalStats(PlayerTableStat playerTableStat) {
        super(playerTableStat);
    }
}
