package com.desapp.football_api.service;

import com.desapp.football_api.aspects.NonCacheable;
import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.exceptions.not_found.UserNotFoundException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieService cookieService;

    @NonCacheable
    public User register(User user, HttpServletResponse response) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("User already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        cookieService.createCookieToResponse(response, savedUser.getUsername());
        return savedUser;
    }

    @NonCacheable
    public User login(User user, HttpServletResponse response) {
        User dbUser = findByUsername(user.getUsername());
        matches(user.getPassword(), dbUser.getPassword());
        cookieService.createCookieToResponse(response, dbUser.getUsername());
        return dbUser;
    }

    @NonCacheable
    public void logout(HttpServletResponse response) {
        cookieService.clearCookieFromResponse(response);
    }

    private void matches(String userPassword, String dbUserPassword) {
        if (!this.passwordEncoder.matches(userPassword, dbUserPassword)) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User update(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new UserNotFoundException(user.getId());
        }
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }

}