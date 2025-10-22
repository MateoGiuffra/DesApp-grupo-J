package com.desapp.football_api.model.stats.player_stats;

import com.desapp.football_api.model.table_stats.TableStat;
import com.desapp.football_api.utils.WhoScoredLink;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@DiscriminatorValue("CURRENT")
@NoArgsConstructor
public class CurrentStats extends PlayerStats {
    public CurrentStats(List<TableStat> tableStats) {
        super(tableStats);
    }

    public CurrentStats(TableStat tableStat) {
        super(tableStat);
    }

    @Override
    public String getPlayerLink(Long playerId) {
        return WhoScoredLink.getCurrentPlayerLink(playerId);
    }
}
