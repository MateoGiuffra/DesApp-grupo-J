package com.desapp.football_api.model.team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdvancedMetrics {
    private Double goalsPerGame;
    private Integer goalsConceded;
    private Double goalsConcededPerGame;
    private Integer goalDifference;
    private Integer cleanSheets;
    private String recentForm;
    private TopPlayerStats topScorer;
    private TopPlayerStats topAssister;
}
