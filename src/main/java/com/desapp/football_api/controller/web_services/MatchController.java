package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Matches", description = "Upcoming fixtures by team")
@AllArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @Operation(
            summary = "Get matches by team",
            description = "Returns the list of matches for the given team id. You can filter by match type (all/past/upcoming) and by location (all/home/away)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of matches returned",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = Match.class)),
                            examples = @ExampleObject(
                                    name = "MatchesArray",
                                    summary = "Example response",
                                    value = "[\n  {\n    \"id\": 1913918,\n    \"date\": \"2025-08-16\",\n    \"time\": \"19:30\",\n    \"homeTeamId\": 51,\n    \"homeTeamName\": \"Mallorca\",\n    \"awayTeamId\": 65,\n    \"awayTeamName\": \"Barcelona\",\n    \"competition\": \"LaLiga\"\n  },\n  {\n    \"id\": 1913888,\n    \"date\": \"2025-08-23\",\n    \"time\": \"21:30\",\n    \"homeTeamId\": 832,\n    \"homeTeamName\": \"Levante\",\n    \"awayTeamId\": 65,\n    \"awayTeamName\": \"Barcelona\",\n    \"competition\": \"LaLiga\"\n  }\n]"
                            )
                    )),
            @ApiResponse(responseCode = "400", description = "Bad request: invalid parameters", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content),
            @ApiResponse(responseCode = "503", description = "Upstream data provider unavailable", content = @Content)
    })
    @GetMapping("/{teamId}")
    public ResponseEntity<List<Match>> getMatchesByTeam(
            @Parameter(description = "External team identifier (e.g., 65 for Barcelona)")
            @PathVariable Long teamId,
            @Parameter(description = "Match type filter: all, past, upcoming", schema = @Schema(implementation = MatchType.class))
            @RequestParam(name = "type", defaultValue = "all") MatchType matchType,
            @Parameter(description = "Match location filter relative to the given team: all, home, away", schema = @Schema(implementation = MatchLocation.class))
            @RequestParam(name = "location", defaultValue = "all") MatchLocation matchLocation
    ) {
        return ResponseEntity.ok(matchService.getMatches(teamId, matchType, matchLocation));
    }


}
