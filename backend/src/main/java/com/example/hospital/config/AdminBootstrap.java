package com.example.hospital.config;

import com.example.hospital.model.AppUser;
import com.example.hospital.model.Role;
import com.example.hospital.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Registration only creates PATIENT accounts, so without this there is no way to get the first ADMIN.
 * Set ADMIN_EMAIL and ADMIN_PASSWORD (min 8 chars) before the first start. Existing accounts are never changed.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;
    private final String name;

    public AdminBootstrap(UserRepository users, PasswordEncoder encoder,
                          @Value("${app.admin.email:}") String email,
                          @Value("${app.admin.password:}") String password,
                          @Value("${app.admin.name:Administrator}") String name) {
        this.users = users;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) return;
        String normalized = email.trim().toLowerCase();
        if (users.existsByEmail(normalized)) return;
        if (password.length() < 8) {
            log.warn("ADMIN_PASSWORD must be at least 8 characters; admin account was not created.");
            return;
        }
        users.save(new AppUser(name, normalized, encoder.encode(password), Role.ADMIN, null));
        log.info("Created initial admin account for {}", normalized);
    }
}
