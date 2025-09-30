package com.desapp.football_api.model.stats;

import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import com.desapp.football_api.utils.WhoScoredLink;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@DiscriminatorValue("CURRENT")
@NoArgsConstructor
public class CurrentStats extends Stats {
    public CurrentStats(List<PlayerTableStat> playerTableStats) {
        super(playerTableStats);
    }

    public CurrentStats(PlayerTableStat playerTableStat) {
        super(playerTableStat);
    }

    @Override
    public String getPlayerLink(Long playerId) {
        return WhoScoredLink.getCurrentPlayerLink(playerId);
    }
}
