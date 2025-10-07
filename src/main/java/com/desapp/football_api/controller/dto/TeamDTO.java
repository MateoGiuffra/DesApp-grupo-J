package com.desapp.football_api.controller.dto;

import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.stats.TeamStats;

import java.util.List;

public record TeamDTO(Long id, String name, TeamStats teamStats, List<Match> matches, List<SimplePlayerDTO> squad) {
    public static TeamDTO fromModel(Team team) {
        List<SimplePlayerDTO> playerDTOs = team.getSquadList()
                .stream()
                .map(SimplePlayerDTO::fromModel)
                .toList();
        return new TeamDTO(team.getId(), team.getName(), team.getStats(), team.getMatches(), playerDTOs);
    }
}

