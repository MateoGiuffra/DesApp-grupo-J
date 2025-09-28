package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.model.player.Player;
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

    // epico
    @Operation(summary = "Search player by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Player.class))}),
            @ApiResponse(responseCode = "404", description = "Player not found", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<Player> getPlayerByName(
            @Parameter(description = "Player name", example = "Lionel Messi")
            @RequestParam String name) throws IOException, InterruptedException {
        return ResponseEntity.ok(playerService.scrapPlayerWithName(name));
    }

    @Operation(summary = "Get player by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Player.class))}),
            @ApiResponse(responseCode = "404", description = "Player not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(
            @Parameter(description = "Player ID", example = "12345")
            @PathVariable Long id) throws IOException, InterruptedException {
        return ResponseEntity.ok(playerService.scrapPlayerWithId(id));
    }
}
