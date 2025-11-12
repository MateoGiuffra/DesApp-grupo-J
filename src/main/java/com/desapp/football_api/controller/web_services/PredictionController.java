package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.model.prediction.PredictionResult;
import com.desapp.football_api.services.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/predictions")
@Tag(name = "Predictions", description = "Endpoints for generating match predictions using Poisson distribution")
@AllArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @GetMapping("/{localTeamId}/{visitorTeamId}")
    @Operation(summary = "Predict match outcome by team IDs",
            description = "Generates a prediction for a match between two teams based on their last 10 games, using a" +
                    " Poisson distribution model. If a team has no historical goal data, a default average strength " +
                    "is used.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Prediction generated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PredictionResult.class))),
                    @ApiResponse(responseCode = "404", description = "One or both teams not found",
                            content = @Content)
            })
    public ResponseEntity<PredictionResult> predictMatch(
            @Parameter(description = "ID of the local team", required = true) @PathVariable Long localTeamId,
            @Parameter(description = "ID of the visitor team", required = true) @PathVariable Long visitorTeamId) {

        return ResponseEntity.ok(predictionService.prediccionPoisson(localTeamId, visitorTeamId));
    }

    @GetMapping
    @Operation(summary = "Predict match outcome by team names",
            description = "Generates a prediction for a match between two teams based on their names. This endpoint" +
                    " uses the same Poisson distribution model as the ID-based prediction.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Prediction generated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = PredictionResult.class))),
                    @ApiResponse(responseCode = "404", description = "One or both teams not found",
                            content = @Content)
            })
    public ResponseEntity<PredictionResult> predictMatchByName(
            @Parameter(description = "Name of the local team", required = true) @RequestParam String localTeamName,
            @Parameter(description = "Name of the visitor team", required = true) @RequestParam String visitorTeamName) {

        return ResponseEntity.ok(predictionService.prediccionPoisson(localTeamName, visitorTeamName));
    }
}
