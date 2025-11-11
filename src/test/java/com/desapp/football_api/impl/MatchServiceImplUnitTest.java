package com.desapp.football_api.impl;

import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.repository.MatchRepository;
import com.desapp.football_api.repository.TeamRepository;
import com.desapp.football_api.services.impl.MatchServiceImpl;
import com.desapp.football_api.services.impl.TeamServiceImpl;
import com.desapp.football_api.services.impl.WhoScoredServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MatchServiceImplUnitTest {

    @Mock
    WhoScoredServiceImpl whoScoredServiceImpl;
    @Mock
    TeamRepository teamRepository;
    @Mock
    TeamServiceImpl teamServiceImpl;
    @Mock
    MatchRepository matchRepository;

    @InjectMocks
    MatchServiceImpl matchServiceImpl;


    @Test
    void getMatches_onError_returnsEmpty() {
        List<Match> matches = matchServiceImpl.getMatches(9L);
        assertTrue(matches.isEmpty());
    }
}
