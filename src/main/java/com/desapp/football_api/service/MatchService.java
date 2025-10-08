package com.desapp.football_api.service;

import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

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
    public List<Match> getMatches(Long teamId) throws IOException, InterruptedException {
        return getMatches(teamId, MatchType.ALL, MatchLocation.ALL);
    }

    public List<Match> getMatches(Long teamId, MatchType matchType, MatchLocation matchLocation) throws IOException, InterruptedException {
        return teamService.getMatches(teamId, matchType, matchLocation);
    }

}
