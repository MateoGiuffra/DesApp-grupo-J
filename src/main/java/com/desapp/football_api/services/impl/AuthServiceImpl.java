package com.desapp.football_api.services.impl;

import com.desapp.football_api.aspects.NonCacheable;
import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.repository.UserRepository;
import com.desapp.football_api.services.AuthService;
import com.desapp.football_api.services.CookieService;
import com.desapp.football_api.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@NonCacheable
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final CookieService cookieServiceImpl;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Override
    public User register(User user, HttpServletResponse response) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("User already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        cookieServiceImpl.createCookieToResponse(response, savedUser.getUsername());
        return savedUser;
    }

    @Override
    public User login(User user, HttpServletResponse response) {
        User dbUser = userService.findByUsername(user.getUsername());
        matches(user.getPassword(), dbUser.getPassword());
        cookieServiceImpl.createCookieToResponse(response, dbUser.getUsername());
        return dbUser;
    }

    @Override
    public void logout(HttpServletResponse response) {
        cookieServiceImpl.clearCookieFromResponse(response);
    }

    private void matches(String userPassword, String dbUserPassword) {
        if (!this.passwordEncoder.matches(userPassword, dbUserPassword)) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }
}
