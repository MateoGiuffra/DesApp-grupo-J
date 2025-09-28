package com.desapp.football_api.controller.dto;

import com.desapp.football_api.model.User;

public record UserRegisterDTO(String username, String password) {
    public static User toModel(UserRegisterDTO userRegisterDTO) {
        return new User(userRegisterDTO.username(), userRegisterDTO.password());
    }
}