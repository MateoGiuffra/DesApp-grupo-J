package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.model.EndpointLog;
import com.desapp.football_api.service.EndpointLogService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@AllArgsConstructor
@Tag(name = "Endpoint Logs", description = "Endpoints to query recorded HTTP access logs for the API")
public class EndpointLogController {
    private final EndpointLogService endpointLogService;

    @Operation(
            summary = "Search logs by user and date range",
            description = "Returns the list of endpoint access logs for the given userId within the inclusive date range. " +
                    "Dates must be provided in ISO format (yyyy-MM-dd)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs found",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EndpointLog.class)),
                            examples = @ExampleObject(
                                    name = "logs-example",
                                    value = "[\n  {\n    \"id\": 101,\n    \"userId\": 42,\n    \"requestPath\": \"/api/teams/66\",\n    \"httpMethod\": \"GET\",\n    \"statusCode\": 200,\n    \"responseContentLength\": 12345,\n    \"responseTime\": 87,\n    \"requestIp\": \"192.168.1.10\",\n    \"timestamp\": \"2025-01-03\"\n  },\n  {\n    \"id\": 102,\n    \"userId\": 42,\n    \"requestPath\": \"/api/players/search?name=Haaland\",\n    \"httpMethod\": \"GET\",\n    \"statusCode\": 200,\n    \"responseContentLength\": 54321,\n    \"responseTime\": 154,\n    \"requestIp\": \"192.168.1.10\",\n    \"timestamp\": \"2025-01-05\"\n  }\n]"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<List<EndpointLog>> getLogsByUserAndDateRange(
            @Parameter(description = "User ID to filter logs by", example = "42")
            @RequestParam("userId") Long userId,
            @Parameter(description = "Start date (inclusive) in ISO format yyyy-MM-dd", example = "2025-01-01")
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive) in ISO format yyyy-MM-dd", example = "2025-01-31")
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<EndpointLog> logs = endpointLogService.findAllByUserIdAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(logs);
    }
}
