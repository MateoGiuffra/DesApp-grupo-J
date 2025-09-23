package com.desapp.football_api.controller.rest;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    @Autowired
    private TeamService teamService;

    @GetMapping("/{id}/squad")
    public ResponseEntity<Team> getPlayersByTeamId(@PathVariable Long id) {
        validateId(id);
        Team team = teamService.getPlayersByTeamId(id);
        return ResponseEntity.ok(team);
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new BadRequestException("Team ID must be a Long value.");
        }
        if (id <= 0) {
            throw new BadRequestException("Team ID must be a positive Long value.");
        }
    }
}
