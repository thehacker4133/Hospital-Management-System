package com.example.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String name,
        @Email @NotBlank @Size(max = 150) String email,
        // BCrypt only uses the first 72 bytes, so longer passwords are rejected instead of silently truncated
        @NotBlank @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters") String password,
        @Pattern(regexp = "^[0-9+()\\-\\s]{0,20}$", message = "Phone number may only contain digits, spaces, + ( ) -") String phone) {}
