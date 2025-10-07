package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Get matches for a team by ID with optional filtering")
    @GetMapping("/{teamId}")
    public ResponseEntity<List<Match>> getMatchesByTeam(
            @PathVariable Long teamId,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "location", required = false) String location
    ) {
        // Keep unit tests/backward compatibility: if no query params provided, call the 1-arg overload (returns ALL)
        if (isBlank(type) && isBlank(location)) {
            return ResponseEntity.ok(matchService.getMatches(teamId));
        }

        MatchType matchType = parseMatchType(type);
        MatchLocation matchLocation = parseMatchLocation(location);
        return ResponseEntity.ok(matchService.getMatches(teamId, matchType, matchLocation));
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static MatchType parseMatchType(String s) {
        if (isBlank(s)) return MatchType.ALL;
        switch (s.trim().toLowerCase()) {
            case "past":
                return MatchType.PAST;
            case "upcoming":
                return MatchType.UPCOMING;
            case "all":
                return MatchType.ALL;
            default:
                throw new IllegalArgumentException("Invalid MatchType: " + s + ". Allowed values: all, past, upcoming");
        }
    }

    private static MatchLocation parseMatchLocation(String s) {
        if (isBlank(s)) return MatchLocation.ALL;
        switch (s.trim().toLowerCase()) {
            case "home":
                return MatchLocation.HOME;
            case "away":
                return MatchLocation.AWAY;
            case "all":
                return MatchLocation.ALL;
            default:
                throw new IllegalArgumentException("Invalid MatchLocation: " + s + ". Allowed values: all, home, away");
        }
    }
}
