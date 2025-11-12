package com.desapp.football_api.model.team;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopPlayerStats {
    private String fullname;
    private int goals;
    private int assists;

}
