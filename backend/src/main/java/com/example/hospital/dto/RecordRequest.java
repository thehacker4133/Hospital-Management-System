package com.example.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordRequest(@NotNull Long patientId, Long appointmentId,
                            @Size(max = 255) String symptoms,
                            @NotBlank @Size(max = 255) String diagnosis,
                            @Size(max = 4000) String notes) {}
