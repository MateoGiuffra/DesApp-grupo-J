package com.desapp.football_api.exceptions.not_found;

import com.desapp.football_api.exceptions.generic.NotFoundException;

public class PlayerNotFoundException extends NotFoundException {
    public PlayerNotFoundException(String id) {
        super("Player with id " + id + " not found.");
    }
}