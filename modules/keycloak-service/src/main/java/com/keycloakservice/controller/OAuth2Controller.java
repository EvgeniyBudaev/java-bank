package com.keycloakservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class OAuth2Controller {

    // Для авторизованных клиентов
    @GetMapping("/auth")
    public ResponseEntity<Void> authenticated() {
        return ResponseEntity.noContent().build();
    }

    // Для авторизованных клиентов с ролью view-profile
    @GetMapping("/secured")
    @Secured("ROLE_view-profile")
    public ResponseEntity<Void> secured() {
        return ResponseEntity.noContent().build();
    }

    // Для авторизованных клиентов с ролью admin
    @GetMapping("/secured/admin")
    @PreAuthorize("hasRole('admin')") // или @Secured("ROLE_admin")
    public ResponseEntity<Void> securedAdmin() {
        return ResponseEntity.noContent().build();
    }

    // Для авторизованных клиентов с ролью user
    @GetMapping("/secured/user")
    @Secured("ROLE_user")
    public ResponseEntity<Void> securedUser() {
        return ResponseEntity.noContent().build();
    }
}
