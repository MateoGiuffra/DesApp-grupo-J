package com.desapp.football_api.service;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.exceptions.generic.UnauthorizedException;
import com.desapp.football_api.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CookieService {
    @Autowired
    private JwtUtil jwtUtil;

    private Cookie createCookie(String jwtToken) {
        Cookie cookie = new Cookie("jwt", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    private Cookie clearCookie() {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    public void createCookieToResponse(HttpServletResponse response, String username) {
        String jwtToken = jwtUtil.generateToken(username);
        Cookie cookie = createCookie(jwtToken);
        response.addCookie(cookie);
    }

    public void clearCookieFromResponse(HttpServletResponse response) {
        Cookie cookie = clearCookie();
        response.addCookie(cookie);
    }

    public void validateTokenAlreadyLogged(String token) {
        if (token != null && jwtUtil.validateToken(token)) {
            throw new BadRequestException("User already logged in");
        }
    }

    public void validateToken(String token) {
        if (token != null && !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException();
        }
    }
}
