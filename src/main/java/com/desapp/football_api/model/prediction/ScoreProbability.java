package com.desapp.football_api.model.prediction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreProbability {
    private int homeGoals;
    private int awayGoals;
    private double probability;
}
