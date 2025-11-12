package com.desapp.football_api.model.prediction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictionResult {
    private Long homeTeamId;
    private Long awayTeamId;
    private double homeWinProbability;
    private double awayWinProbability;
    private double drawProbability;
    private List<ScoreProbability> scoreProbabilities;
}
