package com.placehirex.placementbackend.controller;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Map<String, String> hello(Authentication authentication) {
        String authority = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_STUDENT");
        String role = authority.replace("ROLE_", "");

        String message = "Welcome to PlaceHireX.";
        if ("ADMIN".equals(role)) {
            message = "Welcome ADMIN. You can manage and review platform access.";
        } else if ("STUDENT".equals(role)) {
            message = "Welcome STUDENT. You can view and submit your placement readiness details.";
        }

        return Map.of(
                "message", message,
                "role", role,
                "email", authentication.getName()
        );
    }
}
