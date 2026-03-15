package com.keycloakservice.controller.dto.request;

import jakarta.validation.constraints.Null;

public record RequestUserUpdateDto(
        @Null
        String id,
        @Null
        String username,
        @Null
        String email,
        String firstName,
        String lastName
) {
}
