package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.TeamNotFoundException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.utils.WhoScoredHelper;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            String apiUrl = whoScoredService.getTeamLink(id);
            String body = whoScoredService.fetchJSONString(apiUrl);
            List<Long> playerIds = WhoScoredHelper.getIdsFromResponse(body);

            int threadPoolSize = Math.min(playerIds.size(), 30);
            ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

            List<CompletableFuture<Player>> futures = playerIds.stream()
                    .map(playerId -> CompletableFuture.supplyAsync(() -> {
                        try {
                            String url = whoScoredService.getHistoricalPlayerLink(playerId);
                            String playerBody = whoScoredService.fetchJSONString(url);
                            return playerService.createPlayerFromJSON(playerBody, playerId, StatsType.Historical);
                        } catch (HttpClientErrorException.NotFound | InterruptedException e) {
                            throw new TeamNotFoundException(id);
                        } catch (Exception e) {
                            return null;
                        }
                    }, executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<Player> players = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();

            executor.shutdown();
            return new Team(id, players);

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new TeamNotFoundException(id);
        }
    }

}