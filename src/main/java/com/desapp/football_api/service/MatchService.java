package com.desapp.football_api.service;

import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.utils.WhoScoredHelper;
import com.desapp.football_api.utils.WhoScoredLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
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
        try {
            Team team = teamRepository.findById(teamId).orElse(null);
            if (team == null) return Collections.emptyList();

            String url = WhoScoredLink.getTeamFixturesLink(teamId);
            String body = whoScoredService.fetchJSONString(url);

            List<Match> parsed = WhoScoredHelper.parseFixtures(body, team);
            LocalDate today = LocalDate.now();
            List<Match> upcoming = parsed.stream()
                    .filter(m -> m.getDate() != null && m.getDate().isAfter(today))
                    .toList();

            // Persist: replace previous matches for this team
            matchRepository.deleteByTeam_Id(teamId);
            matchRepository.saveAll(upcoming);
            return upcoming;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<Match> getMatches(Long teamId, MatchType matchType, MatchLocation matchLocation) {
        return teamService.getMatches(teamId, matchType, matchLocation);
    }


}
