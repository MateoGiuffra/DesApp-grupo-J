package com.desapp.football_api.model.comparison;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComparisonData {
    @JsonProperty("goals")
    private ComparisonStats comparisonDataGoals;
    @JsonProperty("rating")
    private ComparisonStats comparisonDataRating;
    @JsonProperty("possession")
    private ComparisonStats comparisonDataPossession;
    @JsonProperty("shotsPerGame")
    private ComparisonStats comparisonDataShotsPerGame;
    @JsonProperty("yellowCards")
    private ComparisonStats comparisonDataYellowCards;

    public ComparisonData() {
        this.comparisonDataGoals = new ComparisonStats("goals");
        this.comparisonDataRating = new ComparisonStats("rating");
        this.comparisonDataPossession = new ComparisonStats("possession");
        this.comparisonDataShotsPerGame = new ComparisonStats("shots per game");
        this.comparisonDataYellowCards = new ComparisonStats("yellow cards");
    }
}
