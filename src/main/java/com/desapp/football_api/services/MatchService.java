package com.desapp.football_api.services;

import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;

import java.util.List;

public interface MatchService {
    List<Match> getMatches(Long teamId);

    List<Match> getMatches(Long teamId, MatchType matchType, MatchLocation matchLocation);
}
