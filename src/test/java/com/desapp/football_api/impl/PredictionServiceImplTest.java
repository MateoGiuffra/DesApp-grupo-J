package com.desapp.football_api.impl;

import com.desapp.football_api.model.PredictionResult;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.services.impl.PredictionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@SpringBootTest
class PredictionServiceImplTest {

    @Autowired
    private PredictionServiceImpl predictionServiceImpl;

    private Team localTeam;
    private Team visitorTeam;

    @BeforeEach
    void setUp() {
        localTeam = new Team();
        localTeam.setId(1L);
        localTeam.setName("Local Team");

        visitorTeam = new Team();
        visitorTeam.setId(2L);
        visitorTeam.setName("Visitor Team");

        List<Match> localMatches = new ArrayList<>();
        List<Match> visitorMatches = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Create 10 past matches for the local team (5 home, 5 away)
        for (int i = 0; i < 10; i++) {
            if (i < 5) { // Home matches
                localMatches.add(new Match(
                        (long) (100 + i),
                        LocalDate.now().minusDays(i + 1).format(formatter),
                        "15:00",
                        1L, "Local Team", 2, // Scored 2
                        (long) (10 + i), "Opponent", 1, // Conceded 1
                        "League",
                        localTeam
                ));
            } else { // Away matches
                localMatches.add(new Match(
                        (long) (100 + i),
                        LocalDate.now().minusDays(i + 1).format(formatter),
                        "15:00",
                        (long) (10 + i), "Opponent", 1, // Conceded 1
                        1L, "Local Team", 2, // Scored 2
                        "League",
                        localTeam
                ));
            }
        }
        localTeam.setMatches(localMatches);

        // Create 10 past matches for the visitor team (5 home, 5 away)
        for (int i = 0; i < 10; i++) {
            if (i < 5) { // Home matches
                visitorMatches.add(new Match(
                        (long) (200 + i),
                        LocalDate.now().minusDays(i + 1).format(formatter),
                        "15:00",
                        2L, "Visitor Team", 1, // Scored 1
                        (long) (20 + i), "Opponent", 1, // Conceded 1
                        "League",
                        visitorTeam
                ));
            } else { // Away matches
                visitorMatches.add(new Match(
                        (long) (200 + i),
                        LocalDate.now().minusDays(i + 1).format(formatter),
                        "15:00",
                        (long) (20 + i), "Opponent", 1, // Conceded 1
                        2L, "Visitor Team", 1, // Scored 1
                        "League",
                        visitorTeam
                ));
            }
        }
        visitorTeam.setMatches(visitorMatches);
    }

    @Test
    void testPrediccionPoisson() {
        // When
        PredictionResult result = predictionServiceImpl.prediccionPoisson(localTeam, visitorTeam);

        // Then
        assertNotNull(result);

        // Check that probabilities sum up to a value close to 1.0
        double totalProbability =
                result.getHomeWinProbability() + result.getAwayWinProbability() + result.getDrawProbability();
        assertTrue(totalProbability > 0.9 && totalProbability <= 1.0000000000000002, "Total probability should be " +
                "close to 1.0, but was " + totalProbability);

        // Check that score probabilities are not empty
        assertFalse(result.getScoreProbabilities().isEmpty());

        // Based on the test data:
        // Local Team: Attack Strength = 2.0, Defense Strength = 1.0
        // Visitor Team: Attack Strength = 1.0, Defense Strength = 1.0
        // Expected goals: Local = 2.0 * 1.0 = 2.0, Visitor = 1.0 * 1.0 = 1.0
        // We expect the home team to have a higher win probability.
        assertTrue(result.getHomeWinProbability() > result.getAwayWinProbability());
        assertTrue(result.getHomeWinProbability() > result.getDrawProbability());
    }
}
