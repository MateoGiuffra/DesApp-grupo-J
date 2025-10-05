package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.model.Match;
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

    @Operation(summary = "Get upcoming matches for a team by ID")
    @GetMapping("/{teamId}")
    public ResponseEntity<List<Match>> getUpcomingByTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(matchService.getUpcomingMatches(teamId));
    }
}
