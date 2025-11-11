package com.desapp.football_api.config;

import com.desapp.football_api.exceptions.not_found.UserNotFoundException;
import com.desapp.football_api.model.User;
import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.services.impl.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserServiceImpl userServiceImpl;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserServiceImpl userServiceImpl) {
        this.jwtUtil = jwtUtil;
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> jwtCookie = Arrays.stream(cookies)
                    .filter(cookie -> "jwt".equals(cookie.getName()))
                    .findFirst();
            if (jwtCookie.isPresent()) {
                token = jwtCookie.get().getValue();
            }
        }

        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsername(token);
            try {
                User user = userServiceImpl.findByUsername(username);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, null, null);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (UserNotFoundException ex) {
                // nothing
            }
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null && isAuthenticationRequired(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":401,\"message\":\"Unauthorized: Invalid credentials.\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticationRequired(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !PublicEndpointsManager.isPublic(path);
    }
}