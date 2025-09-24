package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.TeamNotFoundException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.utils.WhoScoredHelper;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class TeamService {
    @Autowired
    private WhoScoredService whoScoredService;
    @Autowired
    private PlayerService playerService;

    public Team getPlayersByTeamName(@NotEmpty String name) throws IOException {
        String teamId = whoScoredService.getIdFromFirstResult(name, () -> {
            throw new TeamNotFoundException(name);
        });
        return getPlayersByTeamId(Long.valueOf(teamId));
    }

    public Team getPlayersByTeamId(Long id) {
        try {
            String apiUrl = "https://www.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=0&isCurrent=true&playerId=&teamIds=" + id + "&matchId=&stageId=&sortBy=Rating&sortAscending=&age=&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=&timeOfTheGameStart=&isMinApp=false&page=&includeZeroValues=true&numberOfPlayersToPick=&incPens=";
            String body = whoScoredService.fetchJSONString(apiUrl);
            List<Long> playerIds = WhoScoredHelper.getIdsFromResponse(body);
            List<Player> players = new ArrayList<>();

            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(22);
            List<java.util.concurrent.Future<Player>> futures = new java.util.ArrayList<>();

            for (Long playerId : playerIds) {
                String url = "https://www.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=0&isCurrent=false&playerId=" + playerId + "&teamIds=&matchId=&stageId=&tournamentOptions=&sortBy=seasonId&sortAscending=&age=&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=&timeOfTheGameStart=&isMinApp=false&page=&includeZeroValues=true&numberOfPlayersToPick=&incPens=";
                futures.add(executor.submit(() -> {
                    try {
                        String playerBody = whoScoredService.fetchJSONString(url);
                        return playerService.createPlayerFromJSON(playerBody, playerId);
                    } catch (HttpClientErrorException.NotFound | InterruptedException e) {
                        throw new TeamNotFoundException(id);
                    } catch (Exception e) {
                        return null;
                    }
                }));
            }

            for (int i = 0; i < playerIds.size(); i++) {
                try {
                    Player updatedPlayer = futures.get(i).get();
                    if (updatedPlayer != null) {
                        players.add(updatedPlayer);
                    }
                } catch (ExecutionException e) {
                    throw new TeamNotFoundException(id);
                }
            }
            executor.shutdown();
            return new Team(id, players);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TeamNotFoundException(id);
        } catch (HttpClientErrorException.NotFound | IOException ex) {
            throw new TeamNotFoundException(id);
        }
    }
}