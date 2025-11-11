package com.desapp.football_api.services;

import com.desapp.football_api.model.User;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    User register(User user, HttpServletResponse response);

    User login(User user, HttpServletResponse response);

    void logout(HttpServletResponse response);
}
