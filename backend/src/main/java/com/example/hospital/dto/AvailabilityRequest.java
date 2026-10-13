package com.example.hospital.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/** A doctor's complete weekly hours. Sending an empty list clears them (then any 10-minute slot can be booked). */
public record AvailabilityRequest(@NotNull @Size(max = 28) List<@Valid @NotNull Slot> slots) {
    public record Slot(@NotNull DayOfWeek day, @NotNull LocalTime start, @NotNull LocalTime end) {}
}
