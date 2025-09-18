package com.desapp.football_api.model.player;

import com.desapp.football_api.model.table_player_stats.PlayerTableStat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerComplete extends Player {
    private String team;
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


    public PlayerComplete(String name, String positions, String dateOfBirth, String nationality, String team, List<PlayerTableStat> playerTableStats) {
        super(name, positions, dateOfBirth, nationality);
        this.team = team;
        setPlayerResume(playerTableStats);
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

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0 + 0.0001) / 100.0;
    }


}
