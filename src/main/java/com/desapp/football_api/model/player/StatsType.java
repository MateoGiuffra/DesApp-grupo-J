package com.desapp.football_api.model.player;

import com.desapp.football_api.model.stats.ActualStats;
import com.desapp.football_api.model.stats.HistoricalStats;
import com.desapp.football_api.model.table_player_stats.PlayerTableStat;

import java.util.List;

public enum StatsType {
    Historical {
        @Override
        public void setNewInstance(Player player, List<PlayerTableStat> playerTableStats) {
            player.setHistoricalStats(new HistoricalStats(playerTableStats));
        }

        @Override
        public void setNewInstance(Player player, PlayerTableStat playerTableStat) {
            player.setHistoricalStats(new HistoricalStats(playerTableStat));
        }
    },
    Actual {
        @Override
        public void setNewInstance(Player player, List<PlayerTableStat> playerTableStats) {
            player.setActualStats(new ActualStats(playerTableStats));
        }

        @Override
        public void setNewInstance(Player player, PlayerTableStat playerTableStat) {
            player.setActualStats(new ActualStats(playerTableStat));
        }
    };

    public abstract void setNewInstance(Player player, List<PlayerTableStat> playerTableStats);

    public abstract void setNewInstance(Player player, PlayerTableStat playerTableStat);
}