package com.desapp.football_api.controller.DTO;

import com.desapp.football_api.model.User;

public record SimpleUserDTO(Long id, String username) {
    public static SimpleUserDTO fromModel(User user) {
        return new SimpleUserDTO(user.getId(), user.getUsername());
    }

    public static User toModel(SimpleUserDTO simpleUserDTO) {
        return new User(simpleUserDTO.id(), simpleUserDTO.username(), null);
    }
}