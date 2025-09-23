package com.desapp.football_api.controller.rest;

import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    @GetMapping("/search")
    public ResponseEntity<Player> getPlayerByName(@RequestParam String name) throws IOException, InterruptedException {
        return ResponseEntity.ok(playerService.scrapPlayerWithName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable String id) throws IOException, InterruptedException {
        return ResponseEntity.ok(playerService.scrapPlayerWithId(id));
    }
}
