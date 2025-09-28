package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.controller.dto.TeamDTO;
import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Endpoints to query teams and their squad")
public class TeamController {
    @Autowired
    private TeamService teamService;

    @Operation(summary = "Get team by ID", description = "Returns the team and, optionally, only the 'squad' field if ?fields=squad is specified")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TeamDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(
            @Parameter(description = "Team ID", example = "66")
            @PathVariable Long id,
            @Parameter(description = "Filters the response. Use 'squad' to return only the list of players", example = "squad")
            @RequestParam(value = "fields", required = false) String fields) {
        validateId(id);
        Team team = teamService.getPlayersByTeamId(id);
        TeamDTO teamDTO = TeamDTO.fromModel(team);
        return buildTeamResponse(teamDTO, fields);
    }

    @Operation(summary = "Search team by name", description = "Returns the team that matches the given name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = TeamDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<?> getTeamByName(
            @Parameter(description = "Team name", example = "Manchester City")
            @RequestParam String name,
            @Parameter(description = "Filters the response. Use 'squad' to return only the list of players", example = "squad")
            @RequestParam(value = "fields", required = false) String fields) throws IOException {
        Team team = teamService.getPlayersByTeamName(name);
        TeamDTO teamDTO = TeamDTO.fromModel(team);
        return buildTeamResponse(teamDTO, fields);
    }

    private ResponseEntity<?> buildTeamResponse(TeamDTO teamDTO, String fields) {
        if ("squad".equalsIgnoreCase(fields)) {
            return ResponseEntity.ok(teamDTO.squad());
        }
        return ResponseEntity.ok(teamDTO);
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new BadRequestException("Team ID must be a Long value.");
        }
        if (id <= 0) {
            throw new BadRequestException("Team ID must be a positive Long value.");
        }
    }
}
