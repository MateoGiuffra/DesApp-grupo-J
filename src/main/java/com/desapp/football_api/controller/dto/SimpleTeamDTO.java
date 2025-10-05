package com.desapp.football_api.controller.dto;

import com.desapp.football_api.model.Team;

import java.util.List;

public record SimpleTeamDTO(Long id, String name) {
    public static SimpleTeamDTO fromModel(Team team) {
        return new SimpleTeamDTO(team.getId(), team.getName());
    }
}
