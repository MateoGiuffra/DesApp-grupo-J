package com.desapp.football_api.controller;

import com.desapp.football_api.config.SecurityConfig;
import com.desapp.football_api.controller.web_services.UserController;
import com.desapp.football_api.model.User;
import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.services.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserServiceImpl userServiceImpl;

    @MockBean
    JwtUtil jwtUtil;


    @BeforeEach
    void setUp() {
        Mockito.reset(userServiceImpl);
        Mockito.reset(jwtUtil);
    }

    @Test
    @WithMockUser
    void getAll_returnsListOfUsers() throws Exception {
        User u = new User();
        u.setId(3L);
        u.setUsername("mike");
        when(userServiceImpl.getUsersPage(any())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(u)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(3))
                .andExpect(jsonPath("$.content[0].username").value("mike"));
    }

    @Test
    @WithMockUser
    void getById_returnsUser() throws Exception {
        when(userServiceImpl.findById(5L)).thenReturn(new User(5L, "user5", "pass"));

        mockMvc.perform(get("/api/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }
}
