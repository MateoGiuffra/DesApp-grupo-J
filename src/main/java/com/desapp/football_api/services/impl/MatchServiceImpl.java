package com.desapp.football_api.services.impl;

import com.desapp.football_api.aspects.NonCacheable;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.services.MatchService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final TeamServiceImpl teamServiceImpl;

    @Override
    public List<Match> getMatches(Long teamId) {
        return getMatches(teamId, MatchType.ALL, MatchLocation.ALL);
    }

    @Override
    @NonCacheable
    public List<Match> getMatches(Long teamId, MatchType matchType, MatchLocation matchLocation) {
        return teamServiceImpl.getMatches(teamId, matchType, matchLocation);
    }


}