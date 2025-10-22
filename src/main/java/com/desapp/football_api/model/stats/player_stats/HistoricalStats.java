package com.desapp.football_api.model.stats.player_stats;

import com.desapp.football_api.model.table_stats.TableStat;
import com.desapp.football_api.utils.WhoScoredLink;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@DiscriminatorValue("HISTORICAL")
@NoArgsConstructor
public class HistoricalStats extends PlayerStats {
    public HistoricalStats(List<TableStat> tableStats) {
        super(tableStats);
    }

    public HistoricalStats(TableStat tableStat) {
        super(tableStat);
    }

    @Override
    public String getPlayerLink(Long playerId) {
        return WhoScoredLink.getHistoricalPlayerLink(playerId);
    }
}
