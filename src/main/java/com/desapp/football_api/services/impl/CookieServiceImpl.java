package com.desapp.football_api.services.impl;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.exceptions.generic.UnauthorizedException;
import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.services.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class CookieServiceImpl implements CookieService {

    private final JwtUtil jwtUtil;

    private final boolean secureCookie = Boolean.parseBoolean(
            System.getenv().getOrDefault("COOKIE_SECURE", "false")
    );


    private Cookie createCookie(String jwtToken) {
        Cookie cookie = new Cookie("jwt", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        return cookie;
    }

    private Cookie clearCookie() {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookie);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    @Override
    public void createCookieToResponse(HttpServletResponse response, String username) {
        String jwtToken = jwtUtil.generateToken(username);
        Cookie cookie = createCookie(jwtToken);
        response.addCookie(cookie);
    }

    @Override
    public void clearCookieFromResponse(HttpServletResponse response) {
        Cookie cookie = clearCookie();
        response.addCookie(cookie);
    }

    @Override
    public void validateTokenAlreadyLogged(String token) {
        if (token != null && jwtUtil.validateToken(token)) {
            throw new BadRequestException("User already logged in");
        }
    }

    @Override
    public void validateToken(String token) {
        if (token != null && !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException();
        }
    }
}
