package com.keycloakservice.controller.dto.request;

import jakarta.validation.constraints.Null;

public record RequestUserCreateDto(
        @Null
        String username,
        @Null
        String password,
        @Null
        String email,
        String firstName,
        String lastName
) {
}
