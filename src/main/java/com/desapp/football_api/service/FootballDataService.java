package com.desapp.football_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class FootballDataService {
    @Value("${secret.api-key}")
    private String apiKey;
    @Value("${api.football-data}")
    private String baseUrl;
    private static final String HEADER = "X-Auth-Token";

    public Map<String, Object> getBodyResponse(String apiUrl) {
        apiUrl = baseUrl + apiUrl;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER, apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        );
        return response.getBody();
    }


}
