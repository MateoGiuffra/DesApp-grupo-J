package com.desapp.football_api.model.comparison;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamComparison {
    private Long firstTeamId;
    private String firstTeamName;
    private Long secondTeamId;
    private String secondTeamName;
    private ComparisonData comparisonData;
}
