package com.desapp.football_api.config;

public class PublicEndpointsManager {
    protected static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/users/login",
            "/swagger-ui/*",
            "/api/users/register",
            "/api/users/logout",
            "/h2-console/**",
            "/api/players/**"
    };

    public static boolean isPublic(String path) {
        for (String pattern : PUBLIC_ENDPOINTS) {
            if (path.matches(pattern.replace("**", ".*").replace("*", "[^/]*"))) {
                return true;
            }
        }
        return false;
    }
}