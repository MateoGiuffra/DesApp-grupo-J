package com.desapp.football_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FootballDataService {
    @Value("${secret.api-key}")
    private String apiKey;
    @Value("${api.football-data}")
    private String baseUrl;
    private static final String HEADER = "X-Auth-Token";

    public <T> T getBodyResponse(String apiUrl, Class<T> responseType) {
        apiUrl = baseUrl + apiUrl;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER, apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<T> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, responseType);
        return response.getBody();
    }


}
