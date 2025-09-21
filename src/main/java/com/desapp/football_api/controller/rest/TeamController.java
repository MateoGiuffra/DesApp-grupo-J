package com.desapp.football_api.controller.rest;

import com.desapp.football_api.controller.dto.PlayerDTO;
import com.desapp.football_api.service.FootballDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
        ArrayList<PlayerDTO> playerList = new ArrayList<>();

        Object squad = body != null ? body.get("squad") : null;
        if (squad instanceof ArrayList) {
            for (Object playerObj : (ArrayList<?>) squad) {
                if (playerObj instanceof Map) {
                    PlayerDTO player = PlayerDTO.fromFootballData((Map<String, Object>) playerObj);
                    playerList.add(player);
                }
            }
            System.out.println("Fetched " + playerList.size() + " players for team ID " + id);
            return ResponseEntity.ok(playerList);
        }

        return ResponseEntity.ok(squad);
    }
}
