package com.desapp.football_api.controller.REST;

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
    @Value("${secret.api-key}")
    private String API_KEY;
    @GetMapping("/{id}/squad")
    public ResponseEntity<?> getPlayersByTeamId(@PathVariable Long id) {
        String apiUrl = "https://api.football-data.org/v4/teams/" + id;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", API_KEY); // secretApiKey debe ser inyectado desde application.yml
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
        Map body = response.getBody();
        Object squad = body != null ? body.get("squad") : null;
        return ResponseEntity.ok(squad);
    }
}
