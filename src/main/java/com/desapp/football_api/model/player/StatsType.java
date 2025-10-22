package com.desapp.football_api.model.player;

import com.desapp.football_api.model.stats.player_stats.CurrentStats;
import com.desapp.football_api.model.stats.player_stats.HistoricalStats;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;
import com.desapp.football_api.model.table_stats.TableStat;

import java.util.List;

public enum StatsType {
    Historical {
        @Override
        public PlayerStats newInstance(List<TableStat> tableStats) {
            return new HistoricalStats(tableStats);
        }

        @Override
        public PlayerStats newInstance(TableStat tableStat) {
            return new HistoricalStats(tableStat);
        }

        @Override
        public PlayerStats newInstance() {
            return new HistoricalStats();
        }

        @Override
        public Class<? extends PlayerStats> getStatsClass() {
            return HistoricalStats.class;
        }
    },
    Current {
        @Override
        public PlayerStats newInstance(List<TableStat> tableStats) {
            return new CurrentStats(tableStats);
        }

        @Override
        public PlayerStats newInstance(TableStat tableStat) {
            return new CurrentStats(tableStat);
        }

        @Override
        public PlayerStats newInstance() {
            return new CurrentStats();
        }

        @Override
        public Class<? extends PlayerStats> getStatsClass() {
            return CurrentStats.class;
        }
    };

    public abstract PlayerStats newInstance(List<TableStat> tableStats);

    public abstract PlayerStats newInstance();

    public abstract Class<? extends PlayerStats> getStatsClass();

    public abstract PlayerStats newInstance(TableStat tableStat);
}