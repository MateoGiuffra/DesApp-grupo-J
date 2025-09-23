package com.desapp.football_api.model.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimplePlayer {
    private Long id;
    private String fullname;
    private String positions;
    private String dateOfBirth;
    private String nationality;

    public SimplePlayer(Map<String, Object> playerMap) {
        this.fullname = playerMap.containsKey("name") ? (String) playerMap.get("name") : null;
        this.positions = playerMap.containsKey("position") ? (String) playerMap.get("position") : null;
        this.dateOfBirth = playerMap.containsKey("dateOfBirth") ? (String) playerMap.get("dateOfBirth") : null;
        this.nationality = playerMap.containsKey("nationality") ? (String) playerMap.get("nationality") : null;
    }
}
