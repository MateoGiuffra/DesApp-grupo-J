package com.desapp.football_api.controller.rest;

import com.desapp.football_api.model.Player;
import com.desapp.football_api.service.WhoScoredService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    @Autowired
    private WhoScoredService whoScoredService;

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerByName(@PathVariable String id) throws IOException, InterruptedException {
        return ResponseEntity.ok(whoScoredService.scrapPlayerWithId(id));
    }
}
