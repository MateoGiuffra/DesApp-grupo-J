package com.desapp.football_api.model.stats;

import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.desapp.football_api.utils.WhoScoredLink;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@DiscriminatorValue("HISTORICAL")
@NoArgsConstructor
public class HistoricalStats extends Stats {
    public HistoricalStats(List<PlayerTableStat> playerTableStats) {
        super(playerTableStats);
    }

    public HistoricalStats(PlayerTableStat playerTableStat) {
        super(playerTableStat);
    }

    @Override
    public String getPlayerLink(Long playerId) {
        return WhoScoredLink.getHistoricalPlayerLink(playerId);
    }
}
