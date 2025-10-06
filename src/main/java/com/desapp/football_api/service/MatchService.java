package com.desapp.football_api.service;

import com.desapp.football_api.model.Match;
import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.player.StatsType;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.utils.ScrapeHelper;
import com.desapp.football_api.utils.WhoScoredHelper;
import com.desapp.football_api.utils.WhoScoredLink;
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

    public List<Match> getUpcomingMatches(Long teamId) {
        try {
            Team team = ScrapeHelper.getOrScrape(
                    () -> teamRepository.findById(teamId).orElse(null),
                    Objects::isNull,
                    () -> teamService.scrapeTeamByIdAndType(teamId, StatsType.Current)
            );
            return this.scrapeMatchesByTeamId(teamId, team);
        } catch (Exception e) {
            // In case of parsing network errors, return empty list to avoid breaking API
            return List.of();
        }
    }

    public List<Match> scrapeMatchesByTeamId(Long teamId, Team team) throws Exception {
        String url = WhoScoredLink.getTeamFixturesLink(teamId);
        String body = whoScoredService.fetchJSONString(url);

        List<Match> matches = WhoScoredHelper.parseFixtures(body, team);

        matchRepository.deleteByTeam_Id(teamId);
        return matchRepository.saveAll(matches);
    }


}
