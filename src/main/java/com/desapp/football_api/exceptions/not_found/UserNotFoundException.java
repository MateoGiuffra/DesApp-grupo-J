package com.desapp.football_api.exceptions.not_found;

import com.desapp.football_api.exceptions.generic.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found.");
    }

    public UserNotFoundException(String username) {
        super("User with username " + username + " not found.");
    }
}