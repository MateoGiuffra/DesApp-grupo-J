package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.controller.dto.TeamDTO;
import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.services.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Endpoints to query teams and their squad")
@AllArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Get team by ID", description = "Returns the team and, optionally, only the 'squad' field if" +
            " ?fields=squad is specified")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Team found", content =
            {@Content(mediaType = "application/json", schema = @Schema(implementation = TeamDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(@Parameter(description = "Team ID", example = "66") @PathVariable Long id,
                                         @Parameter(description = "Filters the response. Use 'squad' to return only " +
                                                 "the list of players", example = "squad") @RequestParam(value =
                                                 "fields", required = false) String fields, @RequestParam(name =
                    "type", defaultValue = "Current") StatsType type) {
        validateId(id);
        Team team = teamService.getOrScrapeTeamById(id, type);
        TeamDTO teamDTO = TeamDTO.fromModel(team);
        return buildTeamResponse(teamDTO, fields);
    }

    @Operation(summary = "Get advanced stats for a team by ID", description = "Returns advanced statistics for a specific team.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Advanced stats found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)
    })
    @GetMapping("/{id}/advanced-stats")
    public ResponseEntity<?> getAdvancedStats(@Parameter(description = "Team ID", example = "66") @PathVariable Long id, @RequestParam(name = "type", defaultValue = "Current") StatsType type) {
        validateId(id);
        Team team = teamService.getOrScrapeTeamById(id, type);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("id", team.getId());
        response.set("advancedStats", team.getAdvancedStats());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search team by name.", description = "Returns the team that matches the given name")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Team found", content =
            {@Content(mediaType = "application/json", schema = @Schema(implementation = TeamDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)})
    @GetMapping("/search")
    public ResponseEntity<?> getTeamByName(@Parameter(description = "Team name", example = "Manchester City") @RequestParam String name, @Parameter(description = "Filters the response. Use 'squad' to return only the list of players", example = "squad") @RequestParam(value = "fields", required = false) String fields, @RequestParam(name = "type", defaultValue = "Current") StatsType type) {
        try {
            Team team = teamService.getOrScrapeTeamByName(name, type);
            TeamDTO teamDTO = TeamDTO.fromModel(team);
            return buildTeamResponse(teamDTO, fields);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
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
