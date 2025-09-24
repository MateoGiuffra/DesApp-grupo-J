package com.desapp.football_api.controller.dto;

import com.desapp.football_api.model.Team;

import java.util.List;

public record TeamDTO(Long id, List<SimplePlayerDTO> players) {
    public static TeamDTO fromModel(Team team) {
        List<SimplePlayerDTO> playerDTOs = team.getSquadList()
                .stream()
                .map(SimplePlayerDTO::fromModel)
                .toList();
        return new TeamDTO(team.getId(), playerDTOs);
    }
}

