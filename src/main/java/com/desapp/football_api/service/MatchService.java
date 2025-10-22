package com.desapp.football_api.service;

import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class MatchService {

    private final TeamService teamService;

    public List<Match> getMatches(Long teamId) {
        return getMatches(teamId, MatchType.ALL, MatchLocation.ALL);
    }

    public List<Match> getMatches(Long teamId, MatchType matchType, MatchLocation matchLocation) {
        return teamService.getMatches(teamId, matchType, matchLocation);
    }


}