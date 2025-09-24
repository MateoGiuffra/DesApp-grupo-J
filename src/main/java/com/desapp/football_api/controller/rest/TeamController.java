package com.desapp.football_api.controller.rest;

import com.desapp.football_api.controller.dto.TeamDTO;
import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    @Autowired
    private TeamService teamService;

    @GetMapping("/{id}/squad")
    public ResponseEntity<TeamDTO> getPlayersByTeamId(@PathVariable Long id) {
        validateId(id);
        Team team = teamService.getPlayersByTeamId(id);
        return ResponseEntity.ok(TeamDTO.fromModel(team));
    }

    @GetMapping("/search")
    public ResponseEntity<TeamDTO> getPlayersByTeamName(@RequestParam String name) throws IOException {
        Team team = teamService.getPlayersByTeamName(name);
        return ResponseEntity.ok(TeamDTO.fromModel(team));
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
