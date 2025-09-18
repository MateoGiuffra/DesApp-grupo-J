package com.desapp.football_api.model.player;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player implements java.io.Serializable {
    private String fullname;
    private String positions;
    private String dateOfBirth;
    private String nationality;

    public Player(Map<String, Object> playerMap) {
        if (playerMap.containsKey("name")) this.fullname = (String) playerMap.get("name");
        if (playerMap.containsKey("position")) this.positions = (String) playerMap.get("position");
        if (playerMap.containsKey("dateOfBirth")) this.dateOfBirth = (String) playerMap.get("dateOfBirth");
        if (playerMap.containsKey("nationality")) this.nationality = (String) playerMap.get("nationality");
    }


}