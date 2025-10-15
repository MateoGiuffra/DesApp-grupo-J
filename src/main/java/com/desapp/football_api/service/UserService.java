package com.desapp.football_api.service;

import com.desapp.football_api.aspects.NonCacheable;
import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.exceptions.not_found.UserNotFoundException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.repository.UserRepository;
import lombok.AllArgsConstructor;
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

    @NonCacheable
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("User already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @NonCacheable
    public User login(String username) {
        return findByUsername(username);
    }

    public boolean matches(String userPassword, String dbUserPassword) {
        boolean bool = this.passwordEncoder.matches(userPassword, dbUserPassword);
        if (!bool) {
            throw new BadRequestException("Invalid credentials");
        }
        return true;
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

    /**
     * Simple login method example to demonstrate @NonCacheable usage.
     * In this project, authentication/token logic lives in controller/cookie service,
     * so here we just resolve the user by username.
     */

}