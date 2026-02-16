package com.placehirex.placementbackend.service;

import com.placehirex.placementbackend.dto.AuthResponse;
import com.placehirex.placementbackend.dto.LoginRequest;
import com.placehirex.placementbackend.dto.RegisterRequest;
import com.placehirex.placementbackend.model.AppUser;
import com.placehirex.placementbackend.model.Role;
import com.placehirex.placementbackend.repository.AppUserRepository;
import com.placehirex.placementbackend.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalStateException("Email is already registered");
        }

        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        AppUser savedUser = appUserRepository.save(user);

        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(savedUser.getEmail())
                .password(savedUser.getPassword())
                .roles(savedUser.getRole().name())
                .build();
        String token = jwtService.generateToken(principal, savedUser.getRole().name());

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs(),
                savedUser.getRole().name(),
                savedUser.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .orElse(Role.STUDENT.name());
            String token = jwtService.generateToken(userDetails, role);

            return new AuthResponse(
                    token,
                    "Bearer",
                    jwtService.getExpirationMs(),
                    role,
                    userDetails.getUsername()
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}
