package com.desapp.football_api.unit;

import com.desapp.football_api.model.Match;
import com.desapp.football_api.model.Team;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MatchServiceUnitTest {

    @Mock WhoScoredService whoScoredService;
    @Mock TeamRepository teamRepository;
    @Mock TeamService teamService;
    @Mock MatchRepository matchRepository;

    @InjectMocks MatchService matchService;

    @Test
    void getUpcomingMatches_parsesAndFiltersCorrectly() {
        Long teamId = 5L;
        Team team = new Team(teamId, "Team", List.of());
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

        // Build JSON array of arrays as returned by WhoScored (indices as per parseFixtures)
        String today = LocalDate.now().plusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yy"));
        String yesterday = LocalDate.now().minusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yy"));

        String json = "[" +
                // Past match -> should be skipped
                "[1001,0,\"" + yesterday + "\",\"18:00\",10,\"Home\",0,20,\"Away\",0,0,0,0,0,0,0,\"League\"]," +
                // Malformed row (missing id) -> skipped
                "[null,0,\"" + today + "\",\"18:00\",10,\"Home\",0,20,\"Away\",0,0,0,0,0,0,0,\"League\"]," +
                // Bad time format (spaces) -> normalized and accepted
                "[2002,0,\"" + today + "\",\"18: 00\",10,\"Home2\",0,20,\"Away2\",0,0,0,0,0,0,0,\"Cup\"]" +
                "]";

        when(whoScoredService.fetchJSONString(anyString())).thenReturn(json);

        when(matchRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<Match> matches = matchService.getUpcomingMatches(teamId);

        assertEquals(1, matches.size());
        Match m = matches.getFirst();
        assertEquals(2002L, m.getId());
        assertEquals("18:00", m.getTime()); // normalized
        assertEquals("Cup", m.getCompetition());
        assertEquals(team, m.getTeam());

        verify(matchRepository).deleteByTeam_Id(teamId);
        verify(matchRepository).saveAll(anyList());
    }

    @Test
    void getUpcomingMatches_onError_returnsEmpty() {
        when(teamRepository.findById(9L)).thenReturn(Optional.of(new Team(9L, "X", List.of())));
        when(whoScoredService.fetchJSONString(anyString())).thenThrow(new RuntimeException("boom"));
        List<Match> matches = matchService.getUpcomingMatches(9L);
        assertTrue(matches.isEmpty());
    }
}
