package com.desapp.football_api.controller.DTO;

import com.desapp.football_api.model.User;

public record UserRegisterDTO(String username, String password) {
    public static UserRegisterDTO fromModel(String username, String password) {
        return new UserRegisterDTO(username, password);
    }

    public static User toModel(UserRegisterDTO userRegisterDTO) {
        return new User(userRegisterDTO.username(), userRegisterDTO.password());
    }
}