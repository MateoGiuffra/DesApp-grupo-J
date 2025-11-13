package com.desapp.football_api.impl;

import com.desapp.football_api.exceptions.not_found.UserNotFoundException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.repository.UserRepository;
import com.desapp.football_api.services.impl.CookieServiceImpl;
import com.desapp.football_api.services.impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CookieServiceImpl cookieServiceImpl;

    @InjectMocks
    UserServiceImpl userServiceImpl;

    User user;
    HttpServletResponse mockResponse;
    Pageable pageable;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setPassword("plain");
        mockResponse = mock(HttpServletResponse.class);
        pageable = Pageable.unpaged();
    }


    @Test
    void findByUsername_found_returnsUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        assertEquals(user, userServiceImpl.findByUsername("john"));
    }

    @Test
    void findByUsername_missing_throwsNotFound() {
        when(userRepository.findByUsername("x")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.findByUsername("x"));
    }

    @Test
    void existsByUsername_delegatesToRepo() {
        when(userRepository.existsByUsername("john")).thenReturn(true);
        assertTrue(userServiceImpl.existsByUsername("john"));
    }

    @Test
    void getUsersPage_returnsList() {
        when(userRepository.getUsersPage(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));
        assertEquals(1, userServiceImpl.getUsersPage(pageable).getContent().size());
    }

    @Test
    void findById_found_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertEquals(user, userServiceImpl.findById(1L));
    }

    @Test
    void findById_missing_throwsNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.findById(2L));
    }

    @Test
    void update_existing_saves() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(user)).thenReturn(user);
        assertEquals(user, userServiceImpl.update(user));
    }

    @Test
    void update_missing_throwsNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> userServiceImpl.update(user));
    }

    @Test
    void delete_callsRepo() {
        userServiceImpl.delete(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteAll_callsRepo() {
        userServiceImpl.deleteAll();
        verify(userRepository).deleteAll();
    }
}
