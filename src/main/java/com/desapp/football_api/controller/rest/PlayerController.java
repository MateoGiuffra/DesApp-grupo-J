package com.desapp.football_api.controller.rest;

import com.desapp.football_api.service.FootballDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    @Autowired
    private FootballDataService footballDataService;

    @GetMapping("/{id}")
    public ResponseEntity<Map> getPlayerById(@PathVariable Long id) {
        String apiUrl = "/persons/" + id;
        Map body = footballDataService.getBodyResponse(apiUrl, Map.class);
        return ResponseEntity.ok(body);
    }
}
