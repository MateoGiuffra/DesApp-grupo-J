package com.desapp.football_api.services;

import jakarta.servlet.http.HttpServletResponse;

public interface CookieService {
    void createCookieToResponse(HttpServletResponse response, String username);

    void clearCookieFromResponse(HttpServletResponse response);

    void validateTokenAlreadyLogged(String token);

    void validateToken(String token);
}
