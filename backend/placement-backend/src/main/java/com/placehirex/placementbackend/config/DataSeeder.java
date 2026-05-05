package com.placehirex.placementbackend.config;

import com.placehirex.placementbackend.model.AppUser;
import com.placehirex.placementbackend.model.Role;
import com.placehirex.placementbackend.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default admin account on startup if one does not already exist.
 * Also fixes the password if the existing admin has a mismatched hash.
 * Credentials: admin@placehirex.com / admin123
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String ADMIN_EMAIL = "admin@placehirex.com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String ADMIN_NAME = "Admin";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        var existing = appUserRepository.findByEmail(ADMIN_EMAIL);

        if (existing.isPresent()) {
            AppUser admin = existing.get();

            // Fix password if it doesn't match (e.g. was inserted manually with wrong hash)
            if (!passwordEncoder.matches(ADMIN_PASSWORD, admin.getPassword())) {
                admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);
                appUserRepository.save(admin);
                log.info("✓ Admin password was incorrect — reset to 'admin123'");
            } else {
                log.info("Admin account exists with correct password — skipping seed.");
            }
            return;
        }

        AppUser admin = new AppUser();
        admin.setEmail(ADMIN_EMAIL);
        admin.setName(ADMIN_NAME);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        appUserRepository.save(admin);

        log.info("✓ Default admin account created (admin@placehirex.com / admin123)");
    }
}
