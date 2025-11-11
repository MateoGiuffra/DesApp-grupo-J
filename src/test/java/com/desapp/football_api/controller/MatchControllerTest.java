package com.desapp.football_api.controller;

import com.desapp.football_api.controller.web_services.MatchController;
import com.desapp.football_api.model.match.Match;
import com.desapp.football_api.model.match.MatchLocation;
import com.desapp.football_api.model.match.MatchType;
import com.desapp.football_api.services.impl.MatchServiceImpl;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("unit")
@WebMvcTest(MatchController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class MatchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    MatchServiceImpl matchServiceImpl;

    @Test
    void getUpcomingByTeam_returnsMatches() throws Exception {
        Match m = new Match();
        m.setId(1L);
        when(matchServiceImpl.getMatches(10L, MatchType.ALL, MatchLocation.ALL)).thenReturn(List.of(m));

        mockMvc.perform(get("/api/matches/10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getUpcomingByTeam_emptyList_ok() throws Exception {
        when(matchServiceImpl.getMatches(99L, MatchType.ALL, MatchLocation.ALL)).thenReturn(List.of());

        mockMvc.perform(get("/api/matches/99"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
