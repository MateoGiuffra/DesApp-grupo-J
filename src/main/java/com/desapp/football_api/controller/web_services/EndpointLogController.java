package com.desapp.football_api.controller.web_services;

import com.desapp.football_api.model.EndpointLog;
import com.desapp.football_api.service.EndpointLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/logs")
@AllArgsConstructor
@Tag(name = "Endpoint Logs", description = "Endpoints to query recorded HTTP access logs for the API")
public class EndpointLogController {
    private final EndpointLogService endpointLogService;

    @Operation(
            summary = "Search logs by user and date range",
            description = "Returns a paginated list of endpoint access logs for the given userId within the inclusive date range. " +
                    "Dates must be provided in ISO format (yyyy-MM-dd). Pagination parameters like 'page', 'size', and 'sort' are also supported."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "logs-page-example",
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 101,
                                                  "userId": 42,
                                                  "requestPath": "/api/teams/66",
                                                  "httpMethod": "GET",
                                                  "statusCode": 200,
                                                  "responseContentLength": 12345,
                                                  "responseTime": 87,
                                                  "requestIp": "192.168.1.10",
                                                  "timestamp": "2025-01-03"
                                                }
                                              ],
                                              "pageable": {
                                                "sort": {
                                                  "sorted": false,
                                                  "unsorted": true,
                                                  "empty": true
                                                },
                                                "offset": 0,
                                                "pageNumber": 0,
                                                "pageSize": 20,
                                                "paged": true,
                                                "unpaged": false
                                              },
                                              "last": true,
                                              "totalPages": 1,
                                              "totalElements": 1,
                                              "size": 20,
                                              "number": 0,
                                              "sort": {
                                                "sorted": false,
                                                "unsorted": true,
                                                "empty": true
                                              },
                                              "numberOfElements": 1,
                                              "first": true,
                                              "empty": false
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<Page<EndpointLog>> getLogsByUserAndDateRange(
            @Parameter(description = "User ID to filter logs by", example = "42")
            @RequestParam("userId") Long userId,
            @Parameter(description = "Start date (inclusive) in ISO format yyyy-MM-dd", example = "2025-01-01")
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (inclusive) in ISO format yyyy-MM-dd", example = "2025-01-31")
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @ParameterObject Pageable pageable) {
        Page<EndpointLog> logs = endpointLogService.findAllByUserIdAndDateRange(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(logs);
    }
}