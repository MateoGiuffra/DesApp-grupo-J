package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.TeamNotFoundException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.repository.StatsRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.utils.ScrapeHelper;
import com.desapp.football_api.utils.WhoScoredHelper;
import com.desapp.football_api.utils.WhoScoredLink;
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
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private StatsRepository statsRepository;

    public Boolean hasToScrap(Team team, StatsType statsType) {
        return team == null
                || team.getSquadList() == null
                || team.getSquadList().isEmpty()
                || team.getSquadList().stream().anyMatch(player -> player.getStats() == null)
                || team.getSquadList().stream().anyMatch(player -> !(player.getStats().getClass().equals(statsType.getStatsClass())));
    }

    public Team getPlayersByTeamName(@NotEmpty String name, StatsType type) throws IOException, InterruptedException {
        return ScrapeHelper.getOrScrape(() -> getTeamByName(name, type), team -> hasToScrap(team, type), () -> scrapeTeamByNameAndType(name, type));
    }

    public Team getPlayersByTeamId(Long id, StatsType type) throws IOException, InterruptedException {
        return ScrapeHelper.getOrScrape(() -> getTeamById(id, type), team -> hasToScrap(team, type), () -> scrapeTeamByIdAndType(id, type));
    }


    public Team scrapeTeamByNameAndType(String name, StatsType type) {
        String teamId = whoScoredService.getIdFromFirstResult(name, () -> {
            throw new TeamNotFoundException(name);
        });
        try {
            return getPlayersByTeamId(Long.valueOf(teamId), type);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Team scrapeTeamByIdAndType(Long id, StatsType type) {
        try {
            String apiUrl = WhoScoredLink.getTeamLink(id);
            String body = whoScoredService.fetchJSONString(apiUrl);
            String teamName = body.replaceAll(".*?\"teamName\"\\s*:\\s*\"([^\"]+)\".*", "$1");
            List<Long> playerIds = WhoScoredHelper.getIdsFromResponse(body);

            int threadPoolSize = Math.min(playerIds.size(), 30);
            ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

            List<CompletableFuture<Player>> futures = playerIds.stream()
                    .map(playerId -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return playerService.scrapePlayerWithIdAndType(playerId, type);
                        } catch (HttpClientErrorException.NotFound e) {
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
            Team team = new Team(id, teamName, players);
            return teamRepository.save(team);
        } catch (Exception e) {
            throw new TeamNotFoundException(id);
        }
    }

    public Team getTeamByName(String name, StatsType type) {
        Team team = teamRepository.findByName((name)).orElse(null);
        return (getTeamWithPlayers(team, type));
    }

    public Team getTeamById(Long id, StatsType type) {
        Team team = teamRepository.findByIdWithPlayers(id);
        return getTeamWithPlayers(team, type);
    }

    private Team getTeamWithPlayers(Team team, StatsType type) {
        if (team == null) return null;
        for (Player p : team.getSquadList()) {
            statsRepository.findByPlayerIdAndType(p.getId(), type.getStatsClass())
                    .ifPresent(p::setStats);
        }
        return team;
    }


}