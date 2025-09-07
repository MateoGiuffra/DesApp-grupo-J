package com.desapp.football_api.exceptions.bad_request;

import com.desapp.football_api.exceptions.generic.BadRequestException;

public class UserAlreadyExistsException extends BadRequestException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}