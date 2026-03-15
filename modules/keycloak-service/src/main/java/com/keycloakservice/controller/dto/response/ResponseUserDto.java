package com.keycloakservice.controller.dto.response;

import lombok.Data;

@Data
public class ResponseUserDto {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isEnabled;
    private Boolean isEmailVerified;
}
