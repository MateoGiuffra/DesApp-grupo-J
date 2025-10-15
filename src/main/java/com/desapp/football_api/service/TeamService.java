package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.not_found.TeamNotFoundException;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.TeamStats;
import com.desapp.football_api.model.table_stats.TableStat;
import com.desapp.football_api.model.table_stats.TableTeamStats;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.repository.stats.PlayerStatsRepository;
import com.desapp.football_api.utils.ScrapeHelper;
import com.desapp.football_api.utils.WhoScoredHelper;
import com.desapp.football_api.utils.WhoScoredLink;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.desapp.football_api.utils.Normalizer.normalizeName;

@Service
@Transactional
public class TeamService {
    @Autowired
    private WhoScoredService whoScoredService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PlayerStatsRepository playerStatsRepository;
    @Autowired
    private MatchRepository matchRepository;

    public Boolean hasToScrap(Team team, StatsType statsType) {
        Boolean bool = team == null
                || team.getSquadList() == null
                || team.getSquadList().isEmpty()
                || team.getMatches() == null
                || team.getMatches().isEmpty()
                || team.getStats() == null
                || team.hasToBeScrapped()
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

            LocalDate lastTimeScrapped = LocalDate.now();
            Team team = new Team(id, teamName, null, new ArrayList<>(), new ArrayList<>(), lastTimeScrapped);

            this.addPlayersStatsAndMatchesToTeam(id, body, type, team);

            return teamRepository.save(team);

        } catch (Exception e) {
            throw new TeamNotFoundException(id);
        }
    }

    private void addPlayersStatsAndMatchesToTeam(Long id, String body, StatsType type, Team team) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        CompletableFuture<List<Player>> playersFuture =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return scrapePlayersFromTeam(id, body, type, team);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }, executor);
        CompletableFuture<TeamStats> teamStatsFuture = CompletableFuture.supplyAsync(() -> scrapeTeamStatsById(id), executor);
        CompletableFuture<List<Match>> matchesFuture = CompletableFuture.supplyAsync(() -> scrapeTeamMatchesById(id, team), executor);

        List<Player> players = playersFuture.join();
        TeamStats teamStats = teamStatsFuture.join();
        List<Match> matches = matchesFuture.join();

        executor.shutdown();

        team.applyPlayers(players);
        team.applyStats(teamStats);
        team.applyMatches(matches);
    }


    private List<Match> scrapeTeamMatchesById(Long id, Team team) {
        String url = WhoScoredLink.getTeamFixturesLink(id);
        String body = whoScoredService.fetchJSONString(url);
        return WhoScoredHelper.parseFixtures(body, team);
    }

    private TeamStats scrapeTeamStatsById(Long id) {
        String url = WhoScoredLink.getTeamStatsLink(id);
        String response = whoScoredService.fetchJSONString(url);
        return createTeamStatsFromJSON(response, id);
    }

    private TeamStats createTeamStatsFromJSON(String response, Long id) {
        TableTeamStats tableTeamStats = new TableTeamStats(response);
        validateTeamExists(tableTeamStats, id);

        List<TableStat> tableStats = tableTeamStats.getTableStats();
        return new TeamStats(tableStats);
    }

    private void validateTeamExists(TableTeamStats tableTeamStats, Long id) {
        if (!tableTeamStats.teamDoesExist()) {
            throw new TeamNotFoundException(id);
        }
    }

    public List<Player> scrapePlayersFromTeam(Long id, String body, StatsType type, Team team) throws JsonProcessingException {
        List<Long> playerIds = WhoScoredHelper.getIdsFromResponse(body);
        int threadPoolSize = Math.min(playerIds.size(), 30);
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<CompletableFuture<Player>> futures = playerIds.stream()
                .map(playerId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return playerService.createPlayer(playerId, type, team);
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
        return players;
    }


    public Team getTeamByName(String name, StatsType type) {
        try {
            String nameNormalized = normalizeName(name);
            return teamRepository.findByNameAndSquadType(nameNormalized, type.getStatsClass()).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public Team getTeamById(Long id, StatsType type) {
        return teamRepository.findByIdAndSquadType(id, type.getStatsClass()).orElse(null);
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

    public boolean teamDoesExistsAndHasMatches(Long teamId) {
        return teamRepository.existsByIdWithMatches(teamId);
    }

    private List<Match> getFilterMatches(Long teamId, MatchType matchType, MatchLocation matchLocation) {
        if (matchType == MatchType.ALL && matchLocation == MatchLocation.ALL) {
            return matchRepository.findByTeam_Id(teamId);
        }

        LocalDate today = LocalDate.now();
        return teamRepository.findMatchesByTypeAndLocation(teamId, today, matchType.isAfter(), matchLocation.isAtHome());
    }

    public List<Match> getMatches(Long teamId, MatchType matchType, MatchLocation matchLocation) {
        if (this.teamDoesExistsAndHasMatches(teamId)) {
            return this.getFilterMatches(teamId, matchType, matchLocation);
        }
        Team team = this.scrapeTeamByIdAndType(teamId, StatsType.Current);
        return team.getFilterMatches(matchType, matchLocation);
    }
}