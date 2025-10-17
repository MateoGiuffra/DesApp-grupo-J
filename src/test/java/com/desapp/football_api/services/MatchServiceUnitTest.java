package com.desapp.football_api.services;

import com.desapp.football_api.model.Team;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.service.MatchService;
import com.desapp.football_api.service.TeamService;
import com.desapp.football_api.service.WhoScoredService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MatchServiceUnitTest {

    @Mock
    WhoScoredService whoScoredService;
    @Mock
    TeamRepository teamRepository;
    @Mock
    TeamService teamService;
    @Mock
    MatchRepository matchRepository;

    @InjectMocks
    MatchService matchService;


    @Test
    void getMatches_onError_returnsEmpty() {
        when(teamRepository.findById(9L)).thenReturn(Optional.of(new Team(9L, "X", List.of())));
        when(whoScoredService.fetchJSONString(anyString())).thenThrow(new RuntimeException("boom"));
        List<Match> matches = matchService.getMatches(9L);
        assertTrue(matches.isEmpty());
    }
}
