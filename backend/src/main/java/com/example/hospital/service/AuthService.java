package com.example.hospital.service;

import com.example.hospital.dto.AuthRequest;
import com.example.hospital.dto.AuthResponse;
import com.example.hospital.dto.RegisterRequest;
import com.example.hospital.model.AppUser;
import com.example.hospital.model.Role;
import com.example.hospital.exception.ApiException;
import com.example.hospital.repository.UserRepository;
import com.example.hospital.security.JwtService;
import com.example.hospital.security.LoginAttemptTracker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    /** Compared against when the email is unknown, so "no such user" and "wrong password" take similar time. */
    private final String dummyHash;
    private final LoginAttemptTracker attempts;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt,
                       @Value("${app.security.max-login-failures:5}") int maxLoginFailures,
                       @Value("${app.security.lockout-minutes:15}") long lockoutMinutes) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.dummyHash = encoder.encode("not-a-real-password");
        this.attempts = new LoginAttemptTracker(maxLoginFailures, Duration.ofMinutes(lockoutMinutes), Clock.systemUTC());
    }

    public AuthResponse register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new IllegalArgumentException("Email is already registered");
        String phone = r.phone() == null || r.phone().isBlank() ? null : r.phone().trim();
        // Public registration can only ever create PATIENT accounts.
        AppUser user = users.save(new AppUser(r.name().trim(), email, encoder.encode(r.password()), Role.PATIENT, phone));
        return response(user);
    }

    /** @param clientAddress used together with the email to throttle repeated wrong passwords */
    public AuthResponse login(AuthRequest r, String clientAddress) {
        String email = r.email().trim().toLowerCase();
        String key = clientAddress + "|" + email;
        long wait = attempts.secondsBlocked(key);
        if (wait > 0) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed sign-in attempts. Try again in " + ((wait + 59) / 60) + " minute(s).");
        }
        AppUser user = users.findByEmail(email).orElse(null);
        boolean ok = encoder.matches(r.password(), user != null ? user.getPasswordHash() : dummyHash);
        if (user == null || !ok) {
            attempts.recordFailure(key);
            throw new IllegalArgumentException("Invalid email or password");
        }
        attempts.recordSuccess(key);
        return response(user);
    }

    private AuthResponse response(AppUser u) {
        return new AuthResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name(), u.getPhone(), jwt.generate(u.getId()));
    }
}
