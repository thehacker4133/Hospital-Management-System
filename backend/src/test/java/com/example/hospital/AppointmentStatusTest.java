package com.example.hospital;

import com.example.hospital.model.AppointmentStatus;
import org.junit.jupiter.api.Test;

import static com.example.hospital.model.AppointmentStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class AppointmentStatusTest {
    @Test
    void pendingCanBeConfirmedRejectedOrCancelled() {
        assertThat(PENDING.canMoveTo(CONFIRMED)).isTrue();
        assertThat(PENDING.canMoveTo(REJECTED)).isTrue();
        assertThat(PENDING.canMoveTo(CANCELLED)).isTrue();
        assertThat(PENDING.canMoveTo(COMPLETED)).isFalse();
    }

    @Test
    void confirmedCanBeCompletedOrCancelled() {
        assertThat(CONFIRMED.canMoveTo(COMPLETED)).isTrue();
        assertThat(CONFIRMED.canMoveTo(CANCELLED)).isTrue();
        assertThat(CONFIRMED.canMoveTo(REJECTED)).isFalse();
    }

    @Test
    void finalStatesCannotChange() {
        for (AppointmentStatus from : new AppointmentStatus[]{COMPLETED, CANCELLED, REJECTED}) {
            for (AppointmentStatus to : AppointmentStatus.values()) {
                assertThat(from.canMoveTo(to)).isFalse();
            }
        }
    }
}
