package com.placehirex.placementbackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.placehirex.placementbackend.dto.AuthResponse;
import com.placehirex.placementbackend.dto.LoginRequest;
import com.placehirex.placementbackend.dto.RegisterRequest;
import com.placehirex.placementbackend.model.AppUser;
import com.placehirex.placementbackend.model.Role;
import com.placehirex.placementbackend.repository.AppUserRepository;
import com.placehirex.placementbackend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(appUserRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void registerShouldCreateStudentAndReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("student@example.com");
        request.setPassword("password123");

        when(appUserRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(any(), any())).thenReturn("token-value");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("token-value", response.getToken());
        assertEquals("STUDENT", response.getRole());
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("student@example.com");
        request.setPassword("password123");

        when(appUserRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> authService.register(request));
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password123");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                User.withUsername("admin@example.com").password("x").roles("ADMIN").build(),
                null,
                User.withUsername("admin@example.com").password("x").roles("ADMIN").build().getAuthorities()
        );
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);
        assertEquals("jwt-token", response.getToken());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void loginShouldRejectBadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@example.com");
        request.setPassword("bad");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
