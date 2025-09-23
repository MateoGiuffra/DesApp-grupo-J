package com.desapp.football_api.exceptions.not_found;

import com.desapp.football_api.exceptions.generic.NotFoundException;

public class PlayerNotFoundException extends NotFoundException {
    public PlayerNotFoundException(String id) {
        super("No results found for player " + id + ".");
    }

    public PlayerNotFoundException(Long id) {
        super("No results found for player " + id + ".");
    }
}