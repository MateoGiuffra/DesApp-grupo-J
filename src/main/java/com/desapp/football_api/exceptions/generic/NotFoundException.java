package com.desapp.football_api.exceptions.generic;

public class NotFoundException extends CustomRuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
