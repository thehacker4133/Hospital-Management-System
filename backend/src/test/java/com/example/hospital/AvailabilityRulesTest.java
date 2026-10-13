package com.example.hospital;

import com.example.hospital.service.AvailabilityRules;
import com.example.hospital.service.AvailabilityRules.Window;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvailabilityRulesTest {
    private static final LocalTime NINE = LocalTime.of(9, 0);
    private static final LocalTime ONE = LocalTime.of(13, 0);
    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 12);

    private final List<Window> mornings = List.of(new Window(DayOfWeek.MONDAY, NINE, ONE));

    @Test
    void slotsInsideTheWindowAreAvailableAndTheEndIsExclusive() {
        assertThat(AvailabilityRules.isWithin(mornings, MONDAY, NINE)).isTrue();
        assertThat(AvailabilityRules.isWithin(mornings, MONDAY, LocalTime.of(12, 30))).isTrue();
        assertThat(AvailabilityRules.isWithin(mornings, MONDAY, ONE)).isFalse();
        assertThat(AvailabilityRules.isWithin(mornings, MONDAY, LocalTime.of(8, 30))).isFalse();
    }

    @Test
    void otherWeekdaysAreNotAvailable() {
        assertThat(AvailabilityRules.isWithin(mornings, MONDAY.plusDays(1), LocalTime.of(10, 0))).isFalse();
    }

    @Test
    void appointmentStartsUseTenMinuteIncrements() {
        assertThat(AvailabilityRules.isOnSlot(LocalTime.of(10, 0))).isTrue();
        assertThat(AvailabilityRules.isOnSlot(LocalTime.of(10, 10))).isTrue();
        assertThat(AvailabilityRules.isOnSlot(LocalTime.of(10, 20))).isTrue();
        assertThat(AvailabilityRules.isOnSlot(LocalTime.of(10, 30))).isTrue();
        assertThat(AvailabilityRules.isOnSlot(LocalTime.of(10, 15))).isFalse();
        assertThat(AvailabilityRules.isOnSlot(LocalTime.of(10, 0, 5))).isFalse();
    }

    @Test
    void availableTimesExcludeOccupiedStartsAndAreGroupedIntoRanges() {
        List<Window> hours = List.of(new Window(DayOfWeek.MONDAY, NINE, LocalTime.of(10, 0)));
        List<LocalTime> available = AvailabilityRules.availableStartTimes(
                MONDAY, hours, Set.of(LocalTime.of(9, 20)), LocalDateTime.of(2026, 10, 12, 8, 0));

        assertThat(available).containsExactly(
                LocalTime.of(9, 0), LocalTime.of(9, 10), LocalTime.of(9, 30),
                LocalTime.of(9, 40), LocalTime.of(9, 50));
        assertThat(AvailabilityRules.formatStartTimeRanges(available)).isEqualTo("09:00–09:10, 09:30–09:50");
    }

    @Test
    void validScheduleIsAccepted() {
        AvailabilityRules.validate(List.of(
                new Window(DayOfWeek.MONDAY, NINE, ONE),
                new Window(DayOfWeek.MONDAY, ONE, LocalTime.of(17, 0)),
                new Window(DayOfWeek.TUESDAY, NINE, ONE)));
    }

    @Test
    void malformedSchedulesAreRejected() {
        assertThatThrownBy(() -> AvailabilityRules.validate(List.of(new Window(DayOfWeek.MONDAY, ONE, NINE))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AvailabilityRules.validate(List.of(new Window(DayOfWeek.MONDAY, LocalTime.of(9, 5), ONE))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AvailabilityRules.validate(List.of(
                new Window(DayOfWeek.MONDAY, NINE, ONE),
                new Window(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(15, 0)))))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
