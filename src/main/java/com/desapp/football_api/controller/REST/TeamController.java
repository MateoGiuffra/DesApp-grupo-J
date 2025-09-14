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
    @Value("${api.football-data}")
    private String BASE_URL;
    private String HEADER = "X-Auth-Token";


    @GetMapping("/{id}/squad")
    public ResponseEntity<?> getPlayersByTeamId(@PathVariable Long id) {
        String apiUrl = BASE_URL + "/teams/" + id;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER, API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, Map.class);
        Map body = response.getBody();
        Object squad = body != null ? body.get("squad") : null;
        return ResponseEntity.ok(squad);
    }
}
