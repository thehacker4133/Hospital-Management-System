package com.example.hospital;

import com.example.hospital.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void validTokenReturnsUserId() {
        JwtService jwt = new JwtService(SECRET, 60);
        assertThat(jwt.parseUserId(jwt.generate(42L))).contains(42L);
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, 60);
        String token = jwt.generate(42L);
        assertThat(jwt.parseUserId(token + "x")).isEmpty();
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtService other = new JwtService("ffffffffffffffffffffffffffffffff", 60);
        JwtService jwt = new JwtService(SECRET, 60);
        assertThat(jwt.parseUserId(other.generate(1L))).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, -5);
        assertThat(jwt.parseUserId(jwt.generate(1L))).isEmpty();
    }

    @Test
    void garbageIsRejected() {
        assertThat(new JwtService(SECRET, 60).parseUserId("not-a-jwt")).isEmpty();
    }

    @Test
    void shortSecretFailsFast() {
        assertThatThrownBy(() -> new JwtService("too-short", 60)).isInstanceOf(IllegalStateException.class);
    }
}
