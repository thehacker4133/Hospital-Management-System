package com.example.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The admin picks an already-registered user by email; that user becomes a doctor. */
public record DoctorRequest(@NotBlank @Email String email,
                            @Size(max = 100) String specialization,
                            @Size(max = 150) String qualification,
                            @Min(0) @Max(70) Integer experienceYears,
                            Long departmentId,
                            @Size(max = 100) String consultationHours) {}
