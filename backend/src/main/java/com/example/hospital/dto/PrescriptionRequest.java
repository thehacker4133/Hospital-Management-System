package com.example.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PrescriptionRequest(@NotNull Long patientId, Long appointmentId,
                                  @NotBlank @Size(max = 200) String medicine,
                                  @Size(max = 100) String dosage,
                                  @Size(max = 100) String frequency,
                                  @Size(max = 100) String duration,
                                  @Size(max = 2000) String instructions) {}
