package com.desapp.football_api.model.prediction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamStrength {
    private double attackStrength;
    private double defenseStrength;
}
