package com.desapp.football_api.controller.rest;

import com.desapp.football_api.model.Team;
import com.desapp.football_api.service.FootballDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    @Autowired
    private FootballDataService footballDataService;

    @GetMapping("/{id}/squad")
    public ResponseEntity<Team> getPlayersByTeamId(@PathVariable Long id) {
        String apiUrl = "/teams/" + id;
        Map body = footballDataService.getBodyResponse(apiUrl, Map.class);
        Team team = new Team(id, body);
        return ResponseEntity.ok(team);
    }
}
