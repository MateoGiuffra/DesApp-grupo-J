package com.desapp.football_api.model.stats;

import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.desapp.football_api.utils.WhoScoredHelper.roundToTwoDecimals;

@Getter
@Setter
@NoArgsConstructor
public class Stats {
    private int games;
    private int mins;
    private int goals;
    private int assists;
    private int yellowCards;
    private int redCards;
    private double shotsPerGame;
    private double passSuccess;
    private double aerialsWonPerGame;
    private double rating;

    public Stats(List<PlayerTableStat> playerTableStats) {
        setPlayerResume(playerTableStats);
    }

    public Stats(PlayerTableStat playerTableStat) {
        this.goals = playerTableStat.getGoal();
        this.assists = playerTableStat.getAssistTotal();
        this.games = playerTableStat.getApps();
        this.mins = playerTableStat.getMinsPlayed();
        this.yellowCards = (int) playerTableStat.getYellowCard();
        this.redCards = (int) playerTableStat.getRedCard();
        this.shotsPerGame = playerTableStat.getShotsPerGame();
        this.passSuccess = playerTableStat.getPassSuccess();
        this.aerialsWonPerGame = playerTableStat.getAerialWonPerGame();
        this.rating = playerTableStat.getRating();
    }

    public void setPlayerResume(List<PlayerTableStat> playerTableStats) {
        this.games = playerTableStats.stream().mapToInt(PlayerTableStat::getApps).sum();
        this.mins = playerTableStats.stream().mapToInt(PlayerTableStat::getMinsPlayed).sum();
        this.goals = playerTableStats.stream().mapToInt(PlayerTableStat::getGoal).sum();
        this.assists = playerTableStats.stream().mapToInt(PlayerTableStat::getAssistTotal).sum();
        this.yellowCards = (int) playerTableStats.stream().mapToDouble(PlayerTableStat::getYellowCard).sum();
        this.redCards = (int) playerTableStats.stream().mapToDouble(PlayerTableStat::getRedCard).sum();

        List<PlayerTableStat> statsWithMinsForShots = playerTableStats.stream()
                .filter(stat -> stat.getMinsPlayed() > 0)
                .toList();
        double avgShots = statsWithMinsForShots.stream()
                .mapToDouble(PlayerTableStat::getShotsPerGame)
                .average()
                .orElse(0);
        this.shotsPerGame = statsWithMinsForShots.isEmpty() ? 0 : avgShots;

        List<PlayerTableStat> statsWithMins = playerTableStats.stream()
                .filter(stat -> stat.getMinsPlayed() > 0)
                .toList();
        this.passSuccess = statsWithMins.isEmpty() ? 0 :
                roundToTwoDecimals(statsWithMins.stream().mapToDouble(PlayerTableStat::getPassSuccess).sum() / statsWithMins.size());

        this.aerialsWonPerGame = roundToTwoDecimals(
                playerTableStats.stream().mapToDouble(PlayerTableStat::getAerialWonPerGame).average().orElse(0)
        );
        this.rating = roundToTwoDecimals(
                playerTableStats.stream().mapToDouble(PlayerTableStat::getRating)
                        .filter(r -> r > 0).average().orElse(0)
        );
    }


}
