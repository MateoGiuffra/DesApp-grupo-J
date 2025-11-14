package com.desapp.football_api.controller.dto;

import com.desapp.football_api.model.User;

public record UserLoginDTO(String username, String password) {
    public static User toModel(UserLoginDTO userLoginDTO) {
        return new User(null, userLoginDTO.username, userLoginDTO.password);
    }
}

