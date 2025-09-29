package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.CurrentStats;
import com.desapp.football_api.model.stats.HistoricalStats;
import com.desapp.football_api.model.stats.Stats;
import com.desapp.football_api.service.PlayerService;
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
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Players", description = "Search and player details")
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    @Autowired
    private com.desapp.football_api.service.StatsService statsService;

    @Operation(summary = "Search player by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Stats.class))}),
            @ApiResponse(responseCode = "404", description = "Player not found", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<Player> getPlayerByName(
            @Parameter(description = "Player name", example = "Lionel Messi")
            @RequestParam String name) throws IOException, InterruptedException {
        return ResponseEntity.ok(playerService.scrapPlayerWithName(name));
    }

    @Operation(summary = "Get current stats for player by ID; persists if absent")
    @GetMapping("/{id}/current")
    public ResponseEntity<Map<String, Object>> getCurrentById(@PathVariable Long id) throws IOException, InterruptedException {
        Player player = statsService.getOrScrape(id, StatsType.Current);
        return ResponseEntity.ok(buildPlayerResponse(player, StatsType.Current));
    }

    @Operation(summary = "Get historical stats for player by ID; persists if absent")
    @GetMapping("/{id}/historical")
    public ResponseEntity<Map<String, Object>> getHistoricalById(@PathVariable Long id) throws IOException, InterruptedException {
        Player player = statsService.getOrScrape(id, StatsType.Historical);
        return ResponseEntity.ok(buildPlayerResponse(player, StatsType.Historical));
    }

    private Map<String, Object> buildPlayerResponse(Player player, StatsType type) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", player.getId());
        body.put("fullname", player.getFullname());
        body.put("positions", player.getPositions());
        body.put("dateOfBirth", player.getDateOfBirth());
        body.put("nationality", player.getNationality());

        Stats selected = null;
        if (player.getStats() != null) {
            for (Stats s : player.getStats()) {
                if (type == StatsType.Current && s instanceof CurrentStats) { selected = s; break; }
                if (type == StatsType.Historical && s instanceof HistoricalStats) { selected = s; break; }
            }
        }
        body.put("stats", selected);

        // Keep top-level aggregates as implemented in Player getters (prefers current if present)
        body.put("games", player.getGames());
        body.put("goals", player.getGoals());
        body.put("assists", player.getAssists());
        body.put("rating", player.getRating());
        return body;
    }
}
