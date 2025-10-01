package com.desapp.football_api.config;

public class PublicEndpointsManager {

    private PublicEndpointsManager() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    protected static final String[] PUBLIC_ENDPOINTS = {
            // Autenticación de tu API
            "/api/auth/**",
            "/api/users/login",
            "/api/users/register",
            "/api/users/logout",

            // Swagger v3
            "/v3/api-docs",        // importante: exacto
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/webjars/**",

            // Consola H2
            "/h2-console/**",

            // Rutas públicas de tu app
            "/",
            "/index",
            "/index.html",
            "/login",
            "/login.html",
            "/register",
            "/register.html",

            // Recursos estáticos
            "/css/**",
            "/js/**",
            "/assets/**"
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