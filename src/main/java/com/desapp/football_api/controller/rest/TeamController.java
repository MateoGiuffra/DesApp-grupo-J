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

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(@PathVariable Long id, @RequestParam(value = "fields", required = false) String fields) {
        validateId(id);
        Team team = teamService.getPlayersByTeamId(id);
        TeamDTO teamDTO = TeamDTO.fromModel(team);
        if ("squad".equalsIgnoreCase(fields)) {
            return ResponseEntity.ok(teamDTO.squad());
        }
        return ResponseEntity.ok(teamDTO);
    }

    @GetMapping("/search")
    public ResponseEntity<?> getTeamByName(@RequestParam String name, @RequestParam(value = "fields", required = false) String fields) throws IOException {
        Team team = teamService.getPlayersByTeamName(name);
        TeamDTO teamDTO = TeamDTO.fromModel(team);
        if ("squad".equalsIgnoreCase(fields)) {
            return ResponseEntity.ok(teamDTO.squad());
        }
        return ResponseEntity.ok(teamDTO);
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
