package com.placehirex.placementbackend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "R2R3WjBkQ2diVkJ6QzNhbG5rSElQNW4xRjJ3QWlVWW9rVzM4czBhZw==");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 86_400_000L);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        UserDetails user = User.withUsername("student@example.com")
                .password("x")
                .roles("STUDENT")
                .build();

        String token = jwtService.generateToken(user, "STUDENT");

        assertEquals("student@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        UserDetails student = User.withUsername("student@example.com")
                .password("x")
                .roles("STUDENT")
                .build();
        UserDetails otherUser = User.withUsername("other@example.com")
                .password("x")
                .roles("STUDENT")
                .build();

        String token = jwtService.generateToken(student, "STUDENT");
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }
}
