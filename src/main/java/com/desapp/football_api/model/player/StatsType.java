package com.desapp.football_api.model.player;

import com.desapp.football_api.model.stats.CurrentStats;
import com.desapp.football_api.model.stats.HistoricalStats;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;

import java.util.List;

public enum StatsType {
    Historical {
        @Override
        public com.desapp.football_api.model.stats.Stats newInstance(List<PlayerTableStat> playerTableStats) {
            return new HistoricalStats(playerTableStats);
        }

        @Override
        public com.desapp.football_api.model.stats.Stats newInstance(PlayerTableStat playerTableStat) {
            return new HistoricalStats(playerTableStat);
        }
    },
    Current {
        @Override
        public com.desapp.football_api.model.stats.Stats newInstance(List<PlayerTableStat> playerTableStats) {
            return new CurrentStats(playerTableStats);
        }

        @Override
        public com.desapp.football_api.model.stats.Stats newInstance(PlayerTableStat playerTableStat) {
            return new CurrentStats(playerTableStat);
        }
    };

    public abstract com.desapp.football_api.model.stats.Stats newInstance(List<PlayerTableStat> playerTableStats);

    public abstract com.desapp.football_api.model.stats.Stats newInstance(PlayerTableStat playerTableStat);
}