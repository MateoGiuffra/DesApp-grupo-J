package com.desapp.football_api.service;

import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.utils.ScrapeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MatchService {

    @Autowired
    private WhoScoredService whoScoredService;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamService teamService;
    @Autowired
    private MatchRepository matchRepository;

    // Backward-compatible overload used by existing tests/clients
    public List<Match> getMatches(Long teamId) {
        return getMatches(teamId, MatchType.ALL, MatchLocation.ALL);
    }

    public List<Match> getMatches(Long teamId, MatchType matchType, MatchLocation matchLocation) {
        try {
            Team team = ScrapeHelper.getOrScrape(
                    () -> teamRepository.findByIdWithMatches(teamId),
                    Objects::isNull,
                    () -> teamService.scrapeTeamByIdAndType(teamId, StatsType.Current)
            );
            return team.getFilterMatches(matchType, matchLocation);
        } catch (Exception e) {
            // In case of parsing network errors, return empty list to avoid breaking API
            System.out.println("Error fetching matches for team ID " + teamId + ": " + e.getMessage());
            return List.of();
        }
    }

}
