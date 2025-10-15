package com.desapp.football_api.controller.dto;

import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.stats.player_stats.PlayerStats;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record PlayerDTO(
        Long id,
        String fullname,
        String positions,
        String dateOfBirth,
        String nationality,
        Long teamId,
        @JsonUnwrapped PlayerStats playerStats
) {
    public static PlayerDTO fromModel(Player player) {
        return new PlayerDTO(
                player.getId(),
                player.getFullname(),
                player.getPositions(),
                player.getDateOfBirth(),
                player.getNationality(),
                player.getTeamId(),
                player.getStats()
        );
    }
}