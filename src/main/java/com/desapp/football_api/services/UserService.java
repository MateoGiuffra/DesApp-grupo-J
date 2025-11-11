package com.desapp.football_api.services;

import com.desapp.football_api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User findByUsername(String username);

    boolean existsByUsername(String username);

    Page<User> getUsersPage(Pageable pageable);

    User findById(Long id);

    User update(User user);

    void delete(Long id);

    void deleteAll();
}
