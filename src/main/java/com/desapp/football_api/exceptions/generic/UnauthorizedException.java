package com.desapp.football_api.exceptions.generic;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("Unauthorized: Invalid credentials.");
    }
}
