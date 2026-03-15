package com.keycloakservice.controller.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ResponseUserDetailDto {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
    private List<String> groups;
}
