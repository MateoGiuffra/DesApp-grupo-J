package com.desapp.football_api.impl;

import com.desapp.football_api.model.prediction.PredictionResult;
import com.desapp.football_api.model.team.Team;
import com.desapp.football_api.services.impl.PredictionServiceImpl;
import com.desapp.football_api.services.impl.TeamServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PredictionServiceImplTest {

    @Mock
    TeamServiceImpl teamServiceImpl;

    @InjectMocks
    PredictionServiceImpl predictionServiceImpl;

    private Team team(long id) {
        Team t = new Team();
        t.setId(id);
        // Minimal stats/matches; Poisson will fallback to defaults
        return t;
    }

    @Test
    void prediccionPoisson_byIds_returnsConsistentProbabilities() {
        when(teamServiceImpl.getOrScrapeTeamById(1L, com.desapp.football_api.model.player.StatsType.Current))
                .thenReturn(team(1));
        when(teamServiceImpl.getOrScrapeTeamById(2L, com.desapp.football_api.model.player.StatsType.Current))
                .thenReturn(team(2));

        PredictionResult result = predictionServiceImpl.prediccionPoisson(1L, 2L);
        assertNotNull(result);
        assertEquals(1L, result.getHomeTeamId());
        assertEquals(2L, result.getAwayTeamId());
        double sum = result.getHomeWinProbability() + result.getAwayWinProbability() + result.getDrawProbability();
        assertEquals(1.0, sum, 1e-9);
        assertNotNull(result.getScoreProbabilities());
        assertEquals(16, result.getScoreProbabilities().size()); // 0..3 x 0..3
    }

    @Test
    void prediccionPoisson_byNames_returnsResult() {
        when(teamServiceImpl.getOrScrapeTeamByName("A", com.desapp.football_api.model.player.StatsType.Current))
                .thenReturn(team(10));
        when(teamServiceImpl.getOrScrapeTeamByName("B", com.desapp.football_api.model.player.StatsType.Current))
                .thenReturn(team(20));

        PredictionResult result = predictionServiceImpl.prediccionPoisson("A", "B");
        assertNotNull(result);
        assertEquals(10L, result.getHomeTeamId());
        assertEquals(20L, result.getAwayTeamId());
    }
}
