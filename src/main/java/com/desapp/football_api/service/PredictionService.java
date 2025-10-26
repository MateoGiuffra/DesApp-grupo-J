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
    private static final int MAX_GOALS = 3;
    private static final double DEFAULT_STRENGTH = 1.2;

    public PredictionResult prediccionPoisson(Long homeTeamId, Long awayTeamId) throws IOException,
            InterruptedException {
        Team homeTeam = teamService.getOrScrapeTeamById(homeTeamId, StatsType.Current);
        Team awayTeam = teamService.getOrScrapeTeamById(awayTeamId, StatsType.Current);
        return prediccionPoisson(homeTeam, awayTeam);
    }

    // Overload to support direct Team inputs (used by tests and internal calls)
    public PredictionResult prediccionPoisson(Team homeTeam, Team awayTeam) {
        TeamStrength localStrength = calculateTeamStrength(homeTeam);
        TeamStrength visitorStrength = calculateTeamStrength(awayTeam);

        double localExpectedGoals = localStrength.getAttackStrength() * visitorStrength.getDefenseStrength();
        double visitorExpectedGoals = visitorStrength.getAttackStrength() * localStrength.getDefenseStrength();

        List<ScoreProbability> scoreProbabilities = new ArrayList<>();

        // Precompute Poisson for the fixed grid used only to expose scoreProbabilities
        double[] homePoisson = computePoissonProbabilities(localExpectedGoals, MAX_GOALS);
        double[] awayPoisson = computePoissonProbabilities(visitorExpectedGoals, MAX_GOALS);

        for (int homeGoals = 0; homeGoals <= MAX_GOALS; homeGoals++) {
            for (int awayGoals = 0; awayGoals <= MAX_GOALS; awayGoals++) {
                double probability = homePoisson[homeGoals] * awayPoisson[awayGoals];
                scoreProbabilities.add(new ScoreProbability(homeGoals, awayGoals, round(probability)));
            }
        }

        // Compute outcome probabilities independently from the exposed score matrix to avoid truncation bias
        double[] outcomes = computeOutcomeProbabilities(localExpectedGoals, visitorExpectedGoals);
        double homeWinProbability = outcomes[0];
        double awayWinProbability = outcomes[1];
        double drawProbability = outcomes[2];

        // Round and ensure they sum to exactly 1.0 after rounding by adjusting draw minimally
        double homeRounded = round(homeWinProbability);
        double awayRounded = round(awayWinProbability);
        double drawRounded = round(drawProbability);
        double sumRounded = homeRounded + awayRounded + drawRounded;
        if (Math.abs(sumRounded - 1.0) > 1e-9) {
            drawRounded = round(1.0 - homeRounded - awayRounded);
            if (drawRounded < 0) {
                drawRounded = 0.0;
            }
        }

        return new PredictionResult(
                homeRounded,
                awayRounded,
                drawRounded,
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
            attackStrength = totalGoalsScored / lastMatches.size();
            defenseStrength = totalGoalsConceded / lastMatches.size();
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


    // Numerically stable computation of Poisson probabilities up to maxK
    private double[] computePoissonProbabilities(double lambda, int maxK) {
        double safeLambda = (Double.isNaN(lambda) || lambda < 0) ? 0.0 : lambda;
        double[] probs = new double[maxK + 1];
        double p0 = Math.exp(-safeLambda);
        probs[0] = p0;
        double prev = p0;
        for (int k = 1; k <= maxK; k++) {
            prev = prev * (safeLambda / k);
            probs[k] = prev;
        }
        return probs;
    }

    // Compute outcome probabilities using extended ranges until tail mass is negligible
    private double[] computeOutcomeProbabilities(double lambdaHome, double lambdaAway) {
        final double EPS = 1e-6; // tolerance for tail mass per marginal
        final int MAX_CAP = 30;  // hard safety cap

        double[] homeProbs = computePoissonUpToEps(lambdaHome, EPS, MAX_CAP);
        double[] awayProbs = computePoissonUpToEps(lambdaAway, EPS, MAX_CAP);

        int kh = homeProbs.length - 1;
        int ka = awayProbs.length - 1;
        int kmax = Math.max(kh, ka);

        // Build CDFs up to kmax
        double[] homeCdf = new double[kmax + 1];
        double[] awayCdf = new double[kmax + 1];
        double cumH = 0.0, cumA = 0.0;
        for (int k = 0; k <= kmax; k++) {
            if (k <= kh) cumH += homeProbs[k];
            if (k <= ka) cumA += awayProbs[k];
            homeCdf[k] = cumH;
            awayCdf[k] = cumA;
        }

        double pHome = 0.0;
        double pDraw = 0.0;

        // P(X>Y) using Y CDF
        for (int i = 0; i <= kh; i++) {
            double pXi = homeProbs[i];
            if (pXi == 0.0) continue;
            int yIdx = i - 1;
            double pYLe = (yIdx < 0) ? 0.0 : awayCdf[Math.min(yIdx, kmax)];
            pHome += pXi * pYLe;
        }

        // P(X=Y)
        for (int k = 0; k <= Math.min(kh, ka); k++) {
            pDraw += homeProbs[k] * awayProbs[k];
        }

        // P(Y>X) approximately as residual to 1 to keep total consistent with tails
        double pAway = Math.max(0.0, 1.0 - pHome - pDraw);

        // Guard small numerical issues and normalize
        double sum = pHome + pAway + pDraw;
        if (sum <= 0) {
            return new double[]{0.0, 0.0, 1.0};
        }
        pHome /= sum;
        pAway /= sum;
        pDraw /= sum;
        return new double[]{pHome, pAway, pDraw};
    }

    // Poisson probabilities until cumulative mass >= 1 - eps or cap reached
    private double[] computePoissonUpToEps(double lambda, double eps, int cap) {
        double safeLambda = (Double.isNaN(lambda) || lambda < 0) ? 0.0 : lambda;
        List<Double> list = new ArrayList<>();
        double p0 = Math.exp(-safeLambda);
        list.add(p0);
        double cum = p0;
        double prev = p0;
        int k = 1;
        while (cum < 1.0 - eps && k <= cap) {
            prev = prev * (safeLambda / k);
            list.add(prev);
            cum += prev;
            k++;
        }
        // Convert to array
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
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
