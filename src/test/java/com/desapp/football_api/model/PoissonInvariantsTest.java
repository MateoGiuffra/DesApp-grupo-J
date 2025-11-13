package com.desapp.football_api.model;

import com.desapp.football_api.model.prediction.Poisson;
import com.desapp.football_api.model.prediction.PredictionResult;
import com.desapp.football_api.model.team.Team;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class PoissonInvariantsTest {

    @Test
    void prediccionPoisson_probabilitiesAreValidAndSumToOne() {
        Team home = new Team(1L, "Home", null);
        Team away = new Team(2L, "Away", null);

        PredictionResult r = new Poisson().prediccionPoisson(home, away);

        double sum = r.getHomeWinProbability() + r.getAwayWinProbability() + r.getDrawProbability();
        assertEquals(1.0, sum, 1e-9, "Outcome probabilities should sum to 1");
        assertTrue(r.getHomeWinProbability() >= 0 && r.getAwayWinProbability() >= 0 && r.getDrawProbability() >= 0, "No negative probabilities");
        assertNotNull(r.getScoreProbabilities());
        assertEquals(16, r.getScoreProbabilities().size(), "Score matrix expected size: (0..3)x(0..3) -> 16 entries");
    }
}
