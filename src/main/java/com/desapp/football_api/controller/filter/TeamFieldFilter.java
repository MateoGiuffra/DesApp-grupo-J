package com.desapp.football_api.controller.filter;

import com.desapp.football_api.controller.dto.TeamDTO;

import java.util.function.Function;

/**
 * Enum to represent the fields that can be selectively returned for a Team.
 * Each enum constant holds the logic to extract its corresponding field from a TeamDTO.
 */
public enum TeamFieldFilter {
    /**
     * Represents the squad (list of players) of a team.
     */
    SQUAD(TeamDTO::squad);
    // To add more filters in the future, simply add a constant:

    private final Function<TeamDTO, Object> mappingFunction;

    TeamFieldFilter(Function<TeamDTO, Object> mappingFunction) {
        this.mappingFunction = mappingFunction;
    }

    /**
     * Applies the filter logic to a TeamDTO, returning the desired field.
     *
     * @param simpleTeamDTO The data transfer object of the team.
     * @return The specific field extracted from the DTO (e.g., the squad list).
     */
    public Object apply(TeamDTO simpleTeamDTO) {
        return mappingFunction.apply(simpleTeamDTO);
    }
}
