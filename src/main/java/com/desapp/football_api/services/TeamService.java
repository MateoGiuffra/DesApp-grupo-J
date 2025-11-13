package com.desapp.football_api.services;

import com.desapp.football_api.model.comparison.TeamComparison;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.model.player.Player;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.model.team.AdvancedMetrics;
import com.desapp.football_api.model.team.Team;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public interface TeamService {

    Boolean hasToScrap(Team team, StatsType statsType);

    Team getOrScrapeTeamByName(@NotEmpty String name, StatsType type);

    Team getOrScrapeTeamById(Long id, StatsType type);

    Team scrapeTeamByNameAndType(String name, StatsType type);

    Team scrapeTeamByIdAndType(Long id, StatsType type);

    List<Player> scrapePlayersFromTeam(Long id, String body, StatsType type, Team team) throws JsonProcessingException;

    Team getTeamByName(String name, StatsType type);

    Team getTeamById(Long id, StatsType type);

    void updateAllTeamsData();

    boolean teamDoesExistsAndHasMatches(Long teamId);

    List<Match> getMatchesByTeamId(Long teamId, MatchType matchType, MatchLocation matchLocation);

    List<Match> getMatchesByTeamName(String teamName, MatchType matchType, MatchLocation matchLocation);

    AdvancedMetrics getAdvancedMetricsById(Long teamId);

    AdvancedMetrics getAdvancedMetricsByName(String teamName);

    TeamComparison getComparisonByTeamNames(String firstName, String secondName, StatsType statsType);

    TeamComparison getComparisonByTeamIds(Long firstId, Long secondId, StatsType statsType);
}