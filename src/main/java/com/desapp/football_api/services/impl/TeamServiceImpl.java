package com.desapp.football_api.services.impl;

import com.desapp.football_api.exceptions.generic.CustomRuntimeException;
import com.desapp.football_api.exceptions.not_found.TeamNotFoundException;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.stats.TeamStats;
import com.desapp.football_api.model.table_stats.TableStat;
import com.desapp.football_api.model.table_stats.TableTeamStats;
import com.desapp.football_api.model.team.AdvancedMetrics;
import com.desapp.football_api.model.team.Team;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.PlayerRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.repository.stats.PlayerStatsRepository;
import com.desapp.football_api.services.TeamService;
import com.desapp.football_api.utils.ScrapeHelper;
import com.desapp.football_api.utils.WhoScoredHelper;
import com.desapp.football_api.utils.WhoScoredLink;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

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
@AllArgsConstructor
public class TeamServiceImpl implements TeamService {
    private static final Logger logger = LoggerFactory.getLogger(TeamServiceImpl.class);
    private final WhoScoredServiceImpl whoScoredServiceImpl;
    private final PlayerServiceImpl playerServiceImpl;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final PlayerStatsRepository playerStatsRepository;

    @Override
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
        logger.info("Team: has to be scraped: {}", bool);
        return bool;
    }

    @Override
    public Team getOrScrapeTeamByName(@NotEmpty String name, StatsType type) {
        return ScrapeHelper.getOrScrape(() -> getTeamByName(name, type), team -> hasToScrap(team, type),
                () -> scrapeTeamByNameAndType(name, type));
    }

    @Override
    public Team getOrScrapeTeamById(Long id, StatsType type) {
        return ScrapeHelper.getOrScrape(() -> getTeamById(id, type), team -> hasToScrap(team, type),
                () -> scrapeTeamByIdAndType(id, type));
    }


    @Override
    public Team scrapeTeamByNameAndType(String name, StatsType type) {
        String teamId = whoScoredServiceImpl.getIdFromFirstResult(name, () -> {
            throw new TeamNotFoundException(name);
        });
        return getOrScrapeTeamById(Long.valueOf(teamId), type);
    }

    @Override
    public Team scrapeTeamByIdAndType(Long id, StatsType type) {
        try {
            String apiUrl = WhoScoredLink.getTeamLink(id);
            String body = whoScoredServiceImpl.fetchJSONString(apiUrl);
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
                        throw new CustomRuntimeException(e.getMessage());
                    }
                }, executor);
        CompletableFuture<TeamStats> teamStatsFuture = CompletableFuture.supplyAsync(() -> scrapeTeamStatsById(id),
                executor);
        CompletableFuture<List<Match>> matchesFuture = CompletableFuture.supplyAsync(() -> scrapeTeamMatchesById(id,
                team), executor);

        executor.shutdown();

        team.applyPlayers(playersFuture.join());
        team.applyStats(teamStatsFuture.join());
        team.applyMatches(matchesFuture.join());
    }


    private List<Match> scrapeTeamMatchesById(Long id, Team team) {
        String url = WhoScoredLink.getTeamFixturesLink(id);
        String body = whoScoredServiceImpl.fetchJSONString(url);
        return WhoScoredHelper.parseFixtures(body, team);
    }

    private TeamStats scrapeTeamStatsById(Long id) {
        String url = WhoScoredLink.getTeamStatsLink(id);
        String response = whoScoredServiceImpl.fetchJSONString(url);
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

    @Override
    public List<Player> scrapePlayersFromTeam(Long id, String body, StatsType type, Team team) throws JsonProcessingException {
        List<Long> playerIds = WhoScoredHelper.getIdsFromResponse(body);
        int threadPoolSize = Math.min(playerIds.size(), 30);
        ExecutorService executor = null;
        try {
            executor = Executors.newFixedThreadPool(threadPoolSize);
            ExecutorService finalExecutor = executor;
            List<CompletableFuture<Player>> futures = playerIds.stream()
                    .map(playerId -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return playerServiceImpl.createPlayer(playerId, type, team);
                        } catch (HttpClientErrorException.NotFound e) {
                            throw new TeamNotFoundException(id);
                        } catch (Exception e) {
                            return null;
                        }
                    }, finalExecutor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (RuntimeException e) {
            throw new CustomRuntimeException("Failed to scrape players for team ID: " + id);
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }

    @Override
    public Team getTeamByName(String name, StatsType type) {
        try {
            String nameNormalized = normalizeName(name);
            Team team = teamRepository.findByNameAndSquadType(nameNormalized, type.getStatsClass()).orElse(null);
            return getTeamCompleted(team, type);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Team getTeamById(Long id, StatsType type) {
        // First try repository method used by existing unit tests
        Team team = teamRepository.findByIdAndSquadType(id, type.getStatsClass()).orElse(null);
        if (team != null) {
            return team;
        }
        // Fallback: load the team by ID regardless of players' current stats type
        Team baseTeam = teamRepository.findById(id).orElse(null);
        return getTeamCompleted(baseTeam, type);
    }

    private Team getTeamCompleted(Team team, StatsType type) {
        if (team == null) {
            return null;
        }
        // Try to fetch the team where players already reference the requested stats type
        Long id = team.getId();
        team = teamRepository.findByIdWithPlayersAndStatsType(id, type.getStatsClass());
        if (team != null) {
            return team;
        }

        // Load team with players regardless of current stats type
        Team teamWithPlayers = teamRepository.findByIdWithPlayers(id);
        if (teamWithPlayers == null) {
            return null;
        }

        // If ALL players already have persisted stats of the requested type, switch the pointer via single DB update
        boolean allHaveRequestedStats =
                teamWithPlayers.getSquadList() != null && !teamWithPlayers.getSquadList().isEmpty()
                        && playerStatsRepository.countPlayersWithoutStatsOfType(id, type.getStatsClass()) == 0;

        if (allHaveRequestedStats) {
            String discriminator = (type == StatsType.Current) ? "CURRENT" : "HISTORICAL";
            playerRepository.updatePlayersStatsReferenceForTeam(id, discriminator);
            // Reload with the requested stats type attached
            return teamRepository.findByIdWithPlayersAndStatsType(id, type.getStatsClass());
        }
        return null;
    }

    @Override
    public void updateAllTeamsData() {
        teamRepository.findAllIds().forEach(id -> {
            try {
                scrapeTeamByIdAndType(id, StatsType.Current);
                scrapeTeamByIdAndType(id, StatsType.Historical);
            } catch (Exception e) {
                logger.info("Failed to update team with ID: {} - {}", id, e.getMessage());
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
        return teamRepository.findMatchesByTypeAndLocation(teamId, today, matchType.isAfter(),
                matchLocation.isAtHome());
    }

    public List<Match> getMatches(Long teamId, MatchType matchType, MatchLocation matchLocation) {
        if (this.teamDoesExistsAndHasMatches(teamId)) {
            return this.getFilterMatches(teamId, matchType, matchLocation);
        }
        Team team = this.scrapeTeamByIdAndType(teamId, StatsType.Current);
        return team.getFilterMatches(matchType, matchLocation);
    }

    @Override
    public AdvancedMetrics getAdvancedMetricsById(Long teamId) {
        Team team = getOrScrapeTeamById(teamId, StatsType.Current);
        return team.getAdvancedMetrics();
    }

    @Override
    public AdvancedMetrics getAdvancedMetricsByName(String teamName) {
        Team team = getOrScrapeTeamByName(teamName, StatsType.Current);
        return team.getAdvancedMetrics();
    }
}