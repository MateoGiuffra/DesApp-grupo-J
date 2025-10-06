package com.desapp.football_api.model.stats;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.table_stats.TableStat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.desapp.football_api.utils.WhoScoredHelper.roundToTwoDecimals;

@Entity
@DiscriminatorValue("TEAM")
@NoArgsConstructor
@Getter
@Setter
public class TeamStats extends Stats {

    private double possession;

    @OneToOne(mappedBy = "stats")
    @JsonIgnore
    private Team team;

    public TeamStats(List<TableStat> tableStats) {
        super(tableStats);
    }

    @Override
    public void setExtraStats(List<TableStat> tableStats) {
        this.possession = tableStats.stream().mapToDouble(TableStat::getPossession).average().orElse(0) * 100;
        this.possession = roundToTwoDecimals(this.possession);
        this.passSuccess = tableStats.stream().mapToDouble(TableStat::getPassSuccess).average().orElse(0) * 100;
        this.passSuccess = roundToTwoDecimals(this.passSuccess);
        this.shotsPerGame = tableStats.stream().mapToDouble(TableStat::getShotsPerGame).average().orElse(0);
        this.shotsPerGame = roundToTwoDecimals(this.shotsPerGame);
    }

    @Override
    public String getPlayerLink(Long playerId) {
        throw new BadRequestException("A team does not have player stats link");
    }
}
