package com.desapp.football_api.controller.rest;

import com.desapp.football_api.service.FootballDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    @Autowired
    private FootballDataService footballDataService;

    @GetMapping("/{id}/squad")
    public ResponseEntity<?> getPlayersByTeamId(@PathVariable Long id) {
        String apiUrl = "/teams/" + id;
        Map body = footballDataService.getBodyResponse(apiUrl, Map.class);
        Object squad = body != null ? body.get("squad") : null;
        return ResponseEntity.ok(squad);
    }
}
