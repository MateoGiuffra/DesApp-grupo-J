package com.desapp.football_api.model.player;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
@Data
@NoArgsConstructor
public class Player implements java.io.Serializable{
    private String fullname;
    private Integer shirtNumber;
    private String position;
    private String dateOfBirth;
    private String nationality;

    public Player(Map<String, Object> playerMap) {
        if (playerMap.containsKey("firstName")) this.fullname = (String) playerMap.get("firstName");
        if (playerMap.containsKey("lastName")) this.fullname += " " + playerMap.get("lastName");
        if (playerMap.containsKey("position")) this.position = (String) playerMap.get("position");
        if (playerMap.containsKey("dateOfBirth")) this.dateOfBirth = (String) playerMap.get("dateOfBirth");
        if (playerMap.containsKey("nationality")) this.nationality = (String) playerMap.get("nationality");
        if (playerMap.containsKey("shirtNumber")) this.shirtNumber = playerMap.get("shirtNumber") != null ? ((Number) playerMap.get("shirtNumber")).intValue() : null;
    }

}