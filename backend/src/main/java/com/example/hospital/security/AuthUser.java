package com.example.hospital.security;

import com.example.hospital.model.Role;

/** The authenticated caller, available in controllers via @AuthenticationPrincipal. */
public record AuthUser(Long id, String email, Role role) {}
