package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
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

@RestController
@RequestMapping("/api/players")
@Tag(name = "Players", description = "Search and player details")
public class PlayerController {
    @Autowired
    private PlayerService playerService;

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
        return ResponseEntity.ok(playerService.scrapPlayerWithName(name, StatsType.Current));
    }

    @Operation(summary = "Get current stats for player by ID; persists if absent")
    @GetMapping("/{id}/current")
    public ResponseEntity<Player> getCurrentById(@PathVariable Long id) throws IOException, InterruptedException {
        Player player = playerService.getPlayerByIdAndType(id, StatsType.Current);
        return ResponseEntity.ok(player);
    }

    @Operation(summary = "Get historical stats for player by ID; persists if absent")
    @GetMapping("/{id}/historical")
    public ResponseEntity<Player> getHistoricalById(@PathVariable Long id) throws IOException, InterruptedException {
        Player player = playerService.getPlayerByIdAndType(id, StatsType.Historical);
        return ResponseEntity.ok(player);
    }


}
