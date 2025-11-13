package com.desapp.football_api.model.comparison;

import com.desapp.football_api.model.team.Team;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComparisonStats {
    @JsonIgnore
    private String name;
    private Double firstValue;
    private Double secondValue;
    private Double firstTeamVsSecondTeamPercentage;
    private String description;

    public ComparisonStats(String name) {
        this.name = name;
    }

    public void setValues(double firstValue, double secondValue, Team firstTeam, Team secondTeam) {

        this.firstValue = firstValue;
        this.secondValue = secondValue;
        this.firstTeamVsSecondTeamPercentage = calculatePercentage(firstValue, secondValue);
        this.description = generateDescription(firstValue, secondValue, firstTeam, secondTeam);
    }

    private double calculatePercentage(double first, double second) {
        if (second == 0) {
            return first > 0 ? 100.0 : 0.0;
        }
        return ((first - second) / second) * 100;
    }

    private String generateDescription(double first, double second, Team firstTeam, Team secondTeam) {
        double percentage = Math.abs(firstTeamVsSecondTeamPercentage);
        String formattedPercentage = String.format("%.2f%%", percentage);

        if (first > second) {
            return String.format("%s has %s more %s than %s.", firstTeam.getName(), formattedPercentage, name, secondTeam.getName());
        } else if (second > first) {
            return String.format("%s has %s less %s than %s.", firstTeam.getName(), formattedPercentage, name, secondTeam.getName());
        } else {
            return String.format("Both teams have the same amount of %s.", name);
        }
    }
}
