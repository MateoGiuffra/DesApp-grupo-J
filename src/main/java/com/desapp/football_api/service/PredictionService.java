package com.desapp.football_api.service;

import com.desapp.football_api.model.PredictionResult;
import com.desapp.football_api.model.ScoreProbability;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.TeamStats;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PredictionService {
    private final TeamService teamService;
    private static final int MAX_GOALS = 5;
    private static final double DEFAULT_STRENGTH = 1.2;

    public PredictionResult prediccionPoisson(Long homeTeamId, Long awayTeamId) throws IOException,
            InterruptedException {
        Team homeTeam = teamService.getOrScrapeTeamById(homeTeamId, StatsType.Current);
        Team awayTeam = teamService.getOrScrapeTeamById(awayTeamId, StatsType.Current);
        TeamStrength localStrength = calculateTeamStrength(homeTeam);
        TeamStrength visitorStrength = calculateTeamStrength(awayTeam);

        double localExpectedGoals = localStrength.getAttackStrength() * visitorStrength.getDefenseStrength();
        double visitorExpectedGoals = visitorStrength.getAttackStrength() * localStrength.getDefenseStrength();

        List<ScoreProbability> scoreProbabilities = new ArrayList<>();
        double homeWinProbability = 0;
        double awayWinProbability = 0;
        double drawProbability = 0;

        for (int homeGoals = 0; homeGoals <= MAX_GOALS; homeGoals++) {
            for (int awayGoals = 0; awayGoals <= MAX_GOALS; awayGoals++) {
                double probability =
                        poissonDistribution(localExpectedGoals, homeGoals) * poissonDistribution(visitorExpectedGoals
                                , awayGoals);
                scoreProbabilities.add(new ScoreProbability(homeGoals, awayGoals, round(probability)));

                if (homeGoals > awayGoals) {
                    homeWinProbability += probability;
                } else if (awayGoals > homeGoals) {
                    awayWinProbability += probability;
                } else {
                    drawProbability += probability;
                }
            }
        }

        return new PredictionResult(
                round(homeWinProbability),
                round(awayWinProbability),
                round(drawProbability),
                scoreProbabilities
        );
    }

    private TeamStrength calculateTeamStrength(Team team) {
        List<Match> lastMatches = team.getPastMatches().stream()
                .sorted(Comparator.comparing(Match::getDate).reversed())
                .limit(10)
                .collect(Collectors.toList());

        double attackStrength = 0.0;
        double defenseStrength = 0.0;

        if (!lastMatches.isEmpty()) {
            double totalGoalsScored = 0;
            double totalGoalsConceded = 0;
            for (Match match : lastMatches) {
                if (match.getHomeTeamId().equals(team.getId())) {
                    totalGoalsScored += match.getHomeGoals();
                    totalGoalsConceded += match.getAwayGoals();
                } else {
                    totalGoalsScored += match.getAwayGoals();
                    totalGoalsConceded += match.getHomeGoals();
                }
            }
            if (lastMatches.size() > 0) {
                attackStrength = totalGoalsScored / lastMatches.size();
                defenseStrength = totalGoalsConceded / lastMatches.size();
            }
        }

        if (attackStrength == 0.0) {
            TeamStats stats = team.getStats();
            if (stats != null && stats.getGames() > 0 && stats.getGoals() > 0) {
                attackStrength = (double) stats.getGoals() / stats.getGames();
            } else {
                attackStrength = DEFAULT_STRENGTH;
            }
        }

        if (defenseStrength == 0.0) {
            defenseStrength = DEFAULT_STRENGTH;
        }

        return new TeamStrength(attackStrength, defenseStrength);
    }

    private double poissonDistribution(double lambda, int k) {
        return (Math.pow(lambda, k) * Math.exp(-lambda)) / factorial(k);
    }

    private long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Factorial is not defined for negative numbers");
        }
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    private double round(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(4, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static class TeamStrength {
        private final double attackStrength;
        private final double defenseStrength;

        public TeamStrength(double attackStrength, double defenseStrength) {
            this.attackStrength = attackStrength;
            this.defenseStrength = defenseStrength;
        }

        public double getAttackStrength() {
            return attackStrength;
        }

        public double getDefenseStrength() {
            return defenseStrength;
        }
    }
}
