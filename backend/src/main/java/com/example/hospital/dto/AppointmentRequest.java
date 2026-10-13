package com.example.hospital.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/** The patient is always the logged-in user, so there is no patientId here. */
public record AppointmentRequest(@NotNull Long doctorId, @NotNull LocalDate appointmentDate,
                                 @NotNull LocalTime appointmentTime, @Size(max = 255) String reason) {}
