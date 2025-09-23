package com.desapp.football_api.exceptions.not_found;

import com.desapp.football_api.exceptions.generic.NotFoundException;

public class TeamNotFoundException extends NotFoundException {
    public TeamNotFoundException(Long id) {
        super("No results found for team " + id + ".");
    }
}
