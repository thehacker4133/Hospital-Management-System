package com.example.hospital.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusRequest(@NotBlank String status) {}
