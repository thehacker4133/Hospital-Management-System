package com.example.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(@Email @NotBlank @Size(max = 150) String email,
                          @NotBlank @Size(max = 72) String password) {}
