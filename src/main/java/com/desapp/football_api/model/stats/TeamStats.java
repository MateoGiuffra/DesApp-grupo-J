package com.desapp.football_api.model.stats;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.table_stats.TableStat;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import static com.desapp.football_api.utils.WhoScoredHelper.roundToTwoDecimals;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "team_stats")
public class TeamStats extends Stats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;
    private double possession;

    @JsonBackReference
    @OneToOne(mappedBy = "stats")
    @JoinColumn(name = "team_id")
    @JsonIgnore
    @ToString.Exclude
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
