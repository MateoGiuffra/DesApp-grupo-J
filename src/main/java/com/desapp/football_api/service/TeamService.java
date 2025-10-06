package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.TeamNotFoundException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.TeamStats;
import com.desapp.football_api.model.table_stats.TableStat;
import com.desapp.football_api.model.table_stats.TableTeamStats;
import com.desapp.football_api.repository.stats.PlayerStatsRepository;
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
    private PlayerStatsRepository playerStatsRepository;

    public Boolean hasToScrap(Team team, StatsType statsType) {
        Boolean bool = team == null
                || team.getSquadList() == null
                || team.getSquadList().isEmpty()
                || team.getSquadList().stream().anyMatch(player -> player.getStats() == null)
                || team.getSquadList().stream().anyMatch(player -> !(player.getStats().getClass().equals(statsType.getStatsClass())));
        System.out.println("Team: has to be scraped: " + bool);
        return bool;
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
            Team team = scrapePlayersFromTeam(id, teamName, playerIds, type);
            this.scrapeTeamStatsById(id, team);
            return teamRepository.save(team);
        } catch (Exception e) {
            throw new TeamNotFoundException(id);
        }
    }

    private void scrapeTeamStatsById(Long id, Team team) {
        String url = WhoScoredLink.getTeamStatsLink(id);
        String response = whoScoredService.fetchJSONString(url);
        team.setStats(createTeamStatsFromJSON(response, id));
    }

    private TeamStats createTeamStatsFromJSON(String response, Long id) {
        TableTeamStats tableTeamStats = new TableTeamStats(response);
        validateTeamExists(tableTeamStats, id);

        List<TableStat> tableStats = tableTeamStats.getTableStats();
        TeamStats teamStats = new TeamStats();
        teamStats.setResume(tableStats);
        return teamStats;
    };

    private void validateTeamExists(TableTeamStats tableTeamStats, Long id) {

    }

    public Team scrapePlayersFromTeam(Long id, String teamName, List<Long> playerIds, StatsType type) {
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
        return new Team(id, teamName, players);
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
            playerStatsRepository.findByPlayerIdAndType(p.getId(), type.getStatsClass())
                    .ifPresent(p::setStats);
        }
        return team;
    }


    public void updateAllTeamsData() {
        teamRepository.findAllIds().forEach(id -> {
            try {
                scrapeTeamByIdAndType(id, StatsType.Current);
                scrapeTeamByIdAndType(id, StatsType.Historical);
            } catch (Exception e) {
                System.out.println("Failed to update team with ID: " + id + " - " + e.getMessage());
            }
        });
    }
}