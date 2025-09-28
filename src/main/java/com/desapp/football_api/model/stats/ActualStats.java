package com.desapp.football_api.model.stats;

import com.desapp.football_api.model.table_player_stats.PlayerTableStat;

import java.util.List;

public class ActualStats extends Stats {
    public ActualStats(List<PlayerTableStat> playerTableStats) {
        super(playerTableStats);
    }

    public ActualStats(PlayerTableStat playerTableStat) {
        super(playerTableStat);
    }
}
