package com.desapp.football_api.config;

import com.desapp.football_api.security.JwtUtil;
import com.desapp.football_api.service.UserService;
import com.desapp.football_api.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
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
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isPresent()) {
                UserDetails userDetails = (UserDetails) userOpt.get();
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}