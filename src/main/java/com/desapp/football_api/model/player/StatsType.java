package com.desapp.football_api.model.player;

import com.desapp.football_api.model.stats.CurrentStats;
import com.desapp.football_api.model.stats.HistoricalStats;
import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;

import java.util.List;

public enum StatsType {
    Historical {
        @Override
        public Stats newInstance(List<PlayerTableStat> playerTableStats) {
            return new HistoricalStats(playerTableStats);
        }

        @Override
        public Stats newInstance(PlayerTableStat playerTableStat) {
            return new HistoricalStats(playerTableStat);
        }

        @Override
        public Stats newInstance() {
            return new HistoricalStats();
        }

        @Override
        public Class<? extends Stats> getStatsClass() {
            return HistoricalStats.class;
        }
    },
    Current {
        @Override
        public Stats newInstance(List<PlayerTableStat> playerTableStats) {
            return new CurrentStats(playerTableStats);
        }

        @Override
        public Stats newInstance(PlayerTableStat playerTableStat) {
            return new CurrentStats(playerTableStat);
        }

        @Override
        public Stats newInstance() {
            return new CurrentStats();
        }

        @Override
        public Class<? extends Stats> getStatsClass() {
            return CurrentStats.class;
        }
    };

    public abstract Stats newInstance(List<PlayerTableStat> playerTableStats);

    public abstract Stats newInstance();

    public abstract Class<? extends Stats> getStatsClass();

    public abstract Stats newInstance(PlayerTableStat playerTableStat);
}