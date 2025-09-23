package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.TeamNotFoundException;
import com.desapp.football_api.model.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@Service
public class TeamService {
    @Autowired
    private FootballDataService footballDataService;

    public Team getPlayersByTeamId(Long id) {
        try {
            String apiUrl = "/teams/" + id;
            Map<String, Object> body = footballDataService.getBodyResponse(apiUrl);
            return new Team(id, body);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new TeamNotFoundException(id);
        }
    }


}
