package com.example.hospital.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Weekly doctor hours and the appointment slot grid. Plain Java (no Spring) so it can be unit tested.
 * Appointment starts are spaced 10 minutes apart. A window's end is exclusive.
 */
public final class AvailabilityRules {
    public static final int SLOT_MINUTES = 10;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private AvailabilityRules() {}

    public record Window(DayOfWeek day, LocalTime start, LocalTime end) {}

    public static boolean isOnSlot(LocalTime t) {
        return t.getSecond() == 0 && t.getNano() == 0 && t.getMinute() % SLOT_MINUTES == 0;
    }

    /** True if the slot starting at {@code time} on {@code date} lies inside one of the windows. */
    public static boolean isWithin(List<Window> windows, LocalDate date, LocalTime time) {
        for (Window w : windows) {
            if (w.day() == date.getDayOfWeek() && !time.isBefore(w.start()) && time.isBefore(w.end())) return true;
        }
        return false;
    }

    /** Free 10-minute start times inside the doctor's hours, excluding occupied slots and elapsed times today. */
    public static List<LocalTime> availableStartTimes(
            LocalDate date, List<Window> windows, Set<LocalTime> occupied, LocalDateTime now) {
        TreeSet<LocalTime> candidates = new TreeSet<>();
        if (windows.isEmpty()) {
            for (int minute = 0; minute < 24 * 60; minute += SLOT_MINUTES) {
                candidates.add(LocalTime.of(minute / 60, minute % 60));
            }
        } else {
            for (Window window : windows) {
                if (window.day() != date.getDayOfWeek()) continue;
                for (LocalTime time = window.start(); time.isBefore(window.end()); time = time.plusMinutes(SLOT_MINUTES)) {
                    candidates.add(time);
                }
            }
        }

        return candidates.stream()
                .filter(time -> !occupied.contains(time))
                .filter(time -> LocalDateTime.of(date, time).isAfter(now))
                .toList();
    }

    /** Compact consecutive start times into inclusive ranges, e.g. 09:00–09:20 (every 10 minutes). */
    public static String formatStartTimeRanges(List<LocalTime> times) {
        if (times.isEmpty()) return "";
        List<String> ranges = new ArrayList<>();
        LocalTime start = times.get(0);
        LocalTime end = start;
        for (int i = 1; i < times.size(); i++) {
            LocalTime next = times.get(i);
            if (end.plusMinutes(SLOT_MINUTES).equals(next)) {
                end = next;
            } else {
                ranges.add(formatRange(start, end));
                start = end = next;
            }
        }
        ranges.add(formatRange(start, end));
        return String.join(", ", ranges);
    }

    private static String formatRange(LocalTime start, LocalTime end) {
        String first = TIME_FORMAT.format(start);
        return start.equals(end) ? first : first + "–" + TIME_FORMAT.format(end);
    }

    /** Rejects a weekly schedule that is malformed. Throws IllegalArgumentException (HTTP 400) with a readable message. */
    public static void validate(List<Window> windows) {
        for (Window w : windows) {
            if (!isOnSlot(w.start()) || !isOnSlot(w.end())) {
                throw new IllegalArgumentException("Hours must start and end on 10-minute increments");
            }
            if (!w.start().isBefore(w.end())) {
                throw new IllegalArgumentException("Each block must end after it starts");
            }
        }
        for (int i = 0; i < windows.size(); i++) {
            for (int j = i + 1; j < windows.size(); j++) {
                Window a = windows.get(i), b = windows.get(j);
                if (a.day() == b.day() && a.start().isBefore(b.end()) && b.start().isBefore(a.end())) {
                    throw new IllegalArgumentException("Two blocks on the same day overlap");
                }
            }
        }
    }
}
