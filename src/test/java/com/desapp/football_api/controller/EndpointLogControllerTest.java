package com.desapp.football_api.controller;

import com.desapp.football_api.config.SecurityConfig;
import com.desapp.football_api.controller.web_services.EndpointLogController;
import com.desapp.football_api.model.EndpointLog;
import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.services.EndpointLogService;
import com.desapp.football_api.services.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(EndpointLogController.class)
@Import(SecurityConfig.class)
class EndpointLogControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EndpointLogService endpointLogService;

    // Beans requeridos por SecurityConfig
    @MockBean
    JwtUtil jwtUtil;

    @MockBean
    UserServiceImpl userServiceImpl;

    @BeforeEach
    void setup() {
        Mockito.reset(endpointLogService, jwtUtil, userServiceImpl);
    }

    @Test
    @WithMockUser
    void search_returnsPageWithLogs() throws Exception {
        EndpointLog log = new EndpointLog();
        log.setId(1L);
        log.setUserId(42L);
        log.setRequestPath("/api/teams/1");
        log.setHttpMethod("GET");
        log.setStatusCode(200);
        log.setResponseTime("15ms");
        log.setRequestIp("127.0.0.1");
        log.setTimestamp(LocalDate.of(2025, 1, 3));

        Page<EndpointLog> page = new PageImpl<>(List.of(log), PageRequest.of(0, 20), 1);
        when(endpointLogService.findAllByUserIdAndDateRange(eq(42L), eq(LocalDate.of(2025, 1, 1)), eq(LocalDate.of(2025, 1, 31)), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/logs/search")
                        .param("userId", "42")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(42))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
