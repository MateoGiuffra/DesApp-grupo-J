package com.desapp.football_api.controller.dto;

import com.desapp.football_api.model.User;

public record SimpleUserDTO(Long id, String username) {
    public static SimpleUserDTO fromModel(User user) {
        return new SimpleUserDTO(user.getId(), user.getUsername());
    }

}