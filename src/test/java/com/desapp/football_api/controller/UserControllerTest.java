package com.desapp.football_api.controller;

import com.desapp.football_api.config.SecurityConfig;
import com.desapp.football_api.controller.web_services.UserController;
import com.desapp.football_api.model.User;
import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @MockBean
    JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        Mockito.reset(userService);
        Mockito.reset(jwtUtil);
    }

    @Test
    @WithAnonymousUser
    void register_createsUserAndSetsCookie() throws Exception {
        User saved = new User(1L, "john", "encoded_pass");

        when(userService.register(any(User.class), any())).thenReturn(saved);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\",\"password\":\"pass\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    @WithAnonymousUser
    void login_authenticatesAndSetsCookie() throws Exception {
        User dbUser = new User(2L, "alice", "encoded_pass");

        when(userService.login(any(User.class), any())).thenReturn(dbUser);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"x\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    @WithMockUser
    void getAll_returnsListOfUsers() throws Exception {
        User u = new User();
        u.setId(3L);
        u.setUsername("mike");
        when(userService.findAll()).thenReturn(List.of(u));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].username").value("mike"));
    }

    @Test
    @WithMockUser
    void getById_returnsUser() throws Exception {
        when(userService.findById(5L)).thenReturn(new User(5L, "user5", "pass"));

        mockMvc.perform(get("/api/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }


    @Test
    @WithMockUser
    void logout_clearsCookie() throws Exception {
        doNothing().when(userService).logout(any());

        mockMvc.perform(post("/api/users/logout").with(csrf()).cookie(new Cookie("jwt", "tok")))
                .andExpect(status().isOk());
    }
}
