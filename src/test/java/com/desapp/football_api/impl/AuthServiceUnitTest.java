package com.desapp.football_api.impl;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.repository.UserRepository;
import com.desapp.football_api.services.CookieService;
import com.desapp.football_api.services.UserService;
import com.desapp.football_api.services.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CookieService cookieServiceImpl;
    @Mock
    UserService userService;

    @InjectMocks
    AuthServiceImpl userAuthService;

    User user;
    HttpServletResponse mockResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("john");
        user.setPassword("plain");
        mockResponse = mock(HttpServletResponse.class);
    }

    @Test
    void register_uniqueUsername_encodesAndSaves() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(cookieServiceImpl).createCookieToResponse(any(), anyString());

        User saved = userAuthService.register(user, mockResponse);

        assertEquals("hashed", saved.getPassword());
        verify(userRepository).save(any(User.class));
        verify(cookieServiceImpl).createCookieToResponse(mockResponse, "john");
    }

    @Test
    void register_existingUsername_throwsBadRequest() {
        when(userRepository.existsByUsername("john")).thenReturn(true);
        assertThrows(BadRequestException.class, () -> userAuthService.register(user, mockResponse));
        verify(userRepository, never()).save(any());
        verify(cookieServiceImpl, never()).createCookieToResponse(any(), any());
    }

    @Test
    void login_validCredentials_returnsUser() {
        User dbUser = new User(1L, "john", "hashed");
        when(userService.findByUsername("john")).thenReturn(dbUser);
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(true);
        doNothing().when(cookieServiceImpl).createCookieToResponse(any(), anyString());

        User result = userAuthService.login(user, mockResponse);

        assertEquals("john", result.getUsername());
        verify(cookieServiceImpl).createCookieToResponse(mockResponse, "john");
    }

    @Test
    void login_invalidCredentials_throwsBadCredentialsException() {
        User dbUser = new User(1L, "john", "hashed");
        when(userService.findByUsername("john")).thenReturn(dbUser);
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userAuthService.login(user, mockResponse));
        verify(cookieServiceImpl, never()).createCookieToResponse(any(), any());
    }

    @Test
    void logout_clearsCookie() {
        doNothing().when(cookieServiceImpl).clearCookieFromResponse(any());
        userAuthService.logout(mockResponse);
        verify(cookieServiceImpl).clearCookieFromResponse(mockResponse);
    }

}
