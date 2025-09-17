package com.desapp.football_api.controller.rest;

import com.desapp.football_api.service.FootballDataService;
import com.desapp.football_api.service.WhoScoredService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    @Autowired
    private FootballDataService footballDataService;
    @Autowired
    private WhoScoredService whoScoredService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlayerByName(@PathVariable String id) {
        System.out.println("entre");
        try {
            // ids de ejemplo: 419341 (Valentín Barco), 230502 (Lionel Messi), 178 (Cristiano Ronaldo)
            return ResponseEntity.ok(whoScoredService.scrapPlayerWithId(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error fetching player data");
        }
    }
}
