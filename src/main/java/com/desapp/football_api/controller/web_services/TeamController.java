package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.controller.dto.SimpleTeamDTO;
import com.desapp.football_api.controller.dto.TeamDTO;
import com.desapp.football_api.controller.filter.TeamFieldFilter;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.team.AdvancedMetrics;
import com.desapp.football_api.model.team.Team;
import com.desapp.football_api.services.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Endpoints to query teams and their squad")
@AllArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Get team by ID", description = "Returns the team and, optionally, a subset of its fields.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Team found", content =
            {@Content(mediaType = "application/json", schema = @Schema(implementation = SimpleTeamDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(@Parameter(description = "Team ID", example = "65") @PathVariable Long id,
                                         @Parameter(description = "Filters the response to return only specific fields.")
                                         @RequestParam(value = "fields", required = false) TeamFieldFilter fields,
                                         @RequestParam(name = "type", defaultValue = "Current") StatsType type) {
        Team team = teamService.getOrScrapeTeamById(id, type);
        TeamDTO teamDTO = TeamDTO.fromModel(team);
        return buildTeamResponse(teamDTO, fields);
    }

    @Operation(summary = "Get team by name", description = "Returns the team that matches the given name and, optionally, a subset of its fields.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Team found", content =
            {@Content(mediaType = "application/json", schema = @Schema(implementation = TeamDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)})
    @GetMapping
    public ResponseEntity<?> getTeamByName(@Parameter(description = "Team name", example = "Manchester City") @RequestParam String name,
                                           @Parameter(description = "Filters the response to return only specific fields.")
                                           @RequestParam(value = "fields", required = false) TeamFieldFilter fields,
                                           @RequestParam(name = "type", defaultValue = "Current") StatsType type) {
        Team team = teamService.getOrScrapeTeamByName(name, type);
        TeamDTO teamDTO = TeamDTO.fromModel(team);
        return buildTeamResponse(teamDTO, fields);
    }

    @Operation(summary = "Get advanced metrics by team ID", description = "Returns a set of advanced metrics for a specific team by last five matches")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metrics found", content =
                    {@Content(mediaType = "application/json", schema = @Schema(implementation = AdvancedMetrics.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid ID", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)
    })
    @GetMapping("/{id}/advanced-metrics")
    public ResponseEntity<AdvancedMetrics> getAdvancedMetrics(@Parameter(description = "Team ID", example = "65") @Nonnull @Min(0) @PathVariable Long id) {
        return ResponseEntity.ok(teamService.getAdvancedMetricsById(id));
    }

    @Operation(summary = "Get advanced metrics by team name", description = "Returns a set of advanced metrics for a specific team by last five matches")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metrics found", content =
                    {@Content(mediaType = "application/json", schema = @Schema(implementation = AdvancedMetrics.class))}),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content)
    })
    @GetMapping("/advanced-metrics")
    public ResponseEntity<AdvancedMetrics> getAdvancedMetrics(@Parameter(description = "Team name", example = "Manchester City") @RequestParam String name) {
        return ResponseEntity.ok(teamService.getAdvancedMetricsByName(name));
    }

    private ResponseEntity<?> buildTeamResponse(TeamDTO teamDTO, TeamFieldFilter filter) {
        return Optional.ofNullable(filter)
                .map(f -> f.apply(teamDTO))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(teamDTO));
    }
}
