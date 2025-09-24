package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.TeamNotFoundException;
import com.desapp.football_api.model.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

@Service
public class TeamService {
    @Autowired
    private WhoScoredService whoScoredService;

    public Team getPlayersByTeamId(Long id) {
        try {
            String apiUrl = "https://www.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=0&isCurrent=true&playerId=&teamIds=" + id + "&matchId=&stageId=&sortBy=Rating&sortAscending=&age=&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=&timeOfTheGameStart=&isMinApp=false&page=&includeZeroValues=true&numberOfPlayersToPick=&incPens=";
            String body = whoScoredService.fetchJSONString(apiUrl);
            return new Team(id, body);
        } catch (HttpClientErrorException.NotFound | InterruptedException ex) {
            throw new TeamNotFoundException(id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
