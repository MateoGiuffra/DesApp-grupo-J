package com.desapp.football_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictionResult {
    private double homeWinProbability;
    private double awayWinProbability;
    private double drawProbability;
    private List<ScoreProbability> scoreProbabilities;
}
