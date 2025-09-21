package com.desapp.football_api.controller.dto;

import java.util.Map;

public record PlayerDTO(
        String fullname,
        String positions,
        String dateOfBirth,
        String nationality
) {

    public static PlayerDTO fromFootballData(Map<String, Object> playerMap) {
        return new PlayerDTO(
                playerMap.containsKey("name") ? (String) playerMap.get("name") : null,
                playerMap.containsKey("position") ? (String) playerMap.get("position") : null,
                playerMap.containsKey("dateOfBirth") ? (String) playerMap.get("dateOfBirth") : null,
                playerMap.containsKey("nationality") ? (String) playerMap.get("nationality") : null
        );
    }

}
