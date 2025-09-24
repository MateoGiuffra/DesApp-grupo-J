package com.desapp.football_api.controller.dto;

import com.desapp.football_api.model.player.Player;

public record SimplePlayerDTO(
        Long id,
        String fullname,
        Integer assists,
        Integer goals,
        Double rating,
        Integer games
) {
    public static SimplePlayerDTO fromModel(Player player) {
        return new SimplePlayerDTO(
                player.getId(),
                player.getFullname(),
                player.getAssists(),
                player.getGoals(),
                player.getRating(),
                player.getGames()
        );
    }
}