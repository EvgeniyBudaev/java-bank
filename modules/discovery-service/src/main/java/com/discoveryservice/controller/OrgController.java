package com.discoveryservice.controller;

import com.discoveryservice.config.OrganizationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/org")
public class OrgController {
    private final OrganizationProperties properties;

    public OrgController(OrganizationProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/info")
    public OrganizationProperties getOrgInfo() {
        return properties;
    }
}