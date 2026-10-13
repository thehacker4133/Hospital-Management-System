package com.example.hospital.controller;

import com.example.hospital.dto.AuthRequest;
import com.example.hospital.dto.AuthResponse;
import com.example.hospital.dto.RegisterRequest;
import com.example.hospital.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register") public AuthResponse register(@Valid @RequestBody RegisterRequest r) { return auth.register(r); }
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest r, HttpServletRequest request) {
        return auth.login(r, request.getRemoteAddr());
    }
}
