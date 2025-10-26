package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.controller.dto.PlayerDTO;
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
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@Tag(name = "Players", description = "Search and player details")
@AllArgsConstructor
public class PlayerController {
    private final PlayerService playerService;

    @Operation(summary = "Search player by name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player found",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Stats.class))}),
            @ApiResponse(responseCode = "404", description = "Player not found", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<Player> getPlayerByName(@Parameter(description = "Player name", example = "Lionel Messi") @RequestParam String name, @RequestParam(name = "type", defaultValue = "Current") StatsType type) {

        return ResponseEntity.ok(playerService.getPlayerByNameAndType(name, type));
    }

    @Operation(summary = "Get stats for player by ID (current by default); persists if absent")
    @GetMapping("/{id}")
    public ResponseEntity<PlayerDTO> getById(
            @PathVariable Long id,
            @RequestParam(name = "type", defaultValue = "Current") StatsType type
    ) {
        Player player = playerService.getPlayerByIdAndType(id, type);
        return ResponseEntity.ok(PlayerDTO.fromModel(player));
    }

}
