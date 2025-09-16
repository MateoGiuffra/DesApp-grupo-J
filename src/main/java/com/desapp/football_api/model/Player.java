package com.desapp.football_api.model;

import lombok.Data;

import java.util.Map;
@Data
public class Player implements java.io.Serializable{
    private Long id;
    private String firstName;
    private String lastName;
    private String name;
    private String position;
    private String dateOfBirth;
    private String nationality;
    private Integer shirtNumber;
    private Integer marketValue;
    private Contract contract;

    public Player(Map<String, Object> playerMap) {
        if (playerMap.containsKey("id")) this.id = ((Number) playerMap.get("id")).longValue();
        if (playerMap.containsKey("firstName")) this.firstName = (String) playerMap.get("firstName");
        if (playerMap.containsKey("lastName")) this.lastName = (String) playerMap.get("lastName");
        if (playerMap.containsKey("name")) this.name = (String) playerMap.get("name");
        if (playerMap.containsKey("position")) this.position = (String) playerMap.get("position");
        if (playerMap.containsKey("dateOfBirth")) this.dateOfBirth = (String) playerMap.get("dateOfBirth");
        if (playerMap.containsKey("nationality")) this.nationality = (String) playerMap.get("nationality");
        if (playerMap.containsKey("shirtNumber")) this.shirtNumber = playerMap.get("shirtNumber") != null ? ((Number) playerMap.get("shirtNumber")).intValue() : null;
        if (playerMap.containsKey("marketValue")) this.marketValue = playerMap.get("marketValue") != null ? ((Number) playerMap.get("marketValue")).intValue() : null;
        if (playerMap.containsKey("contract")) this.contract = (Contract) playerMap.get("contract");
    }

}