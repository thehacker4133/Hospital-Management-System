package com.example.hospital.model;

public enum AppointmentStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED, REJECTED;

    /** Allowed lifecycle: PENDING -> CONFIRMED/REJECTED/CANCELLED, CONFIRMED -> COMPLETED/CANCELLED. Everything else is final. */
    public boolean canMoveTo(AppointmentStatus next) {
        return switch (this) {
            case PENDING -> next == CONFIRMED || next == REJECTED || next == CANCELLED;
            case CONFIRMED -> next == COMPLETED || next == CANCELLED;
            default -> false;
        };
    }
}
