package com.desapp.football_api.exceptions.generic;

public class BadRequestException extends CustomRuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
