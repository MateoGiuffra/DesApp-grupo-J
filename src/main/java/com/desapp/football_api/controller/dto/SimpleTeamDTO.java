package com.desapp.football_api.controller.dto;

import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.stats.TeamStats;
import com.desapp.football_api.model.team.Team;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

public record SimpleTeamDTO(Long id, String name, @JsonUnwrapped TeamStats teamStats, List<Match> matches,
                            List<SimplePlayerDTO> squad) {
    public static SimpleTeamDTO fromModel(Team team) {
        List<SimplePlayerDTO> playerDTOs = team.getSquadList()
                .stream()
                .map(SimplePlayerDTO::fromModel)
                .toList();
        return new SimpleTeamDTO(
                team.getId(),
                team.getName(),
                team.getStats(),
                team.getMatches(),
                playerDTOs
        );
    }
}

