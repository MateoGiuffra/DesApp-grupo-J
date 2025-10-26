package com.desapp.football_api.exceptions.generic;

public class UnauthorizedException extends CustomRuntimeException {
    public UnauthorizedException() {
        super("Unauthorized: Invalid credentials.");
    }
}
