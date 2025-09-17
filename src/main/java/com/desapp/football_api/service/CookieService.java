package com.desapp.football_api.service;

import com.desapp.football_api.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CookieService {
    @Autowired
    private JwtUtil jwtUtil;

    public Cookie createCookie(String jwtToken) {
        Cookie cookie = new Cookie("jwt", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public Cookie clearCookie() {
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

}
