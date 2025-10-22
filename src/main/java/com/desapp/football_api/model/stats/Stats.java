package com.desapp.football_api.model.stats;

import com.desapp.football_api.model.table_stats.TableStat;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

import static com.desapp.football_api.utils.WhoScoredHelper.roundToTwoDecimals;

@ToString
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public abstract class Stats {
    private int games;
    private int goals;
    private int yellowCards;
    private int redCards;
    protected double shotsPerGame;
    protected double passSuccess;
    private double aerialsWonPerGame;
    private double rating;

    public Stats(List<TableStat> tableStats) {
        setResume(tableStats);
    }

    public Stats(TableStat tableStat) {
        this.goals = tableStat.getGoal();
        this.games = tableStat.getApps();
        this.yellowCards = (int) tableStat.getYellowCard();
        this.redCards = (int) tableStat.getRedCard();
        this.shotsPerGame = tableStat.getShotsPerGame();
        this.passSuccess = tableStat.getPassSuccess();
        this.aerialsWonPerGame = tableStat.getAerialWonPerGame();
        this.rating = tableStat.getRating();
    }

    public void setResume(List<TableStat> tableStats) {
        this.games = tableStats.stream().mapToInt(TableStat::getApps).sum();
        this.goals = tableStats.stream().mapToInt(TableStat::getGoal).sum();
        this.yellowCards = (int) tableStats.stream().mapToDouble(TableStat::getYellowCard).sum();
        this.redCards = (int) tableStats.stream().mapToDouble(TableStat::getRedCard).sum();
        this.aerialsWonPerGame = roundToTwoDecimals(
                tableStats.stream().mapToDouble(TableStat::getAerialWonPerGame).average().orElse(0)
        );
        this.rating = roundToTwoDecimals(
                tableStats.stream().mapToDouble(TableStat::getRating)
                        .filter(r -> r > 0).average().orElse(0)
        );
        this.setExtraStats(tableStats);
    }

    public abstract void setExtraStats(List<TableStat> tableStats);

    public abstract String getPlayerLink(Long playerId);
}
