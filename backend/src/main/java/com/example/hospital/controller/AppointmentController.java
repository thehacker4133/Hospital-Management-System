package com.example.hospital.controller;

import com.example.hospital.dto.AppointmentRequest;
import com.example.hospital.dto.StatusRequest;
import com.example.hospital.dto.Views;
import com.example.hospital.exception.ApiException;
import com.example.hospital.model.*;
import com.example.hospital.repository.AppointmentRepository;
import com.example.hospital.repository.DoctorAvailabilityRepository;
import com.example.hospital.repository.DoctorRepository;
import com.example.hospital.repository.UserRepository;
import com.example.hospital.security.AuthUser;
import com.example.hospital.service.AvailabilityRules;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController @RequestMapping("/api/appointments")
public class AppointmentController {
    /** Cancelled or rejected appointments free the time slot again. */
    private static final Set<AppointmentStatus> SLOT_FREE = Set.of(AppointmentStatus.CANCELLED, AppointmentStatus.REJECTED);

    private final AppointmentRepository appointments;
    private final UserRepository users;
    private final DoctorRepository doctors;
    private final DoctorAvailabilityRepository availability;

    public AppointmentController(AppointmentRepository appointments, UserRepository users, DoctorRepository doctors,
                                 DoctorAvailabilityRepository availability) {
        this.appointments = appointments;
        this.users = users;
        this.doctors = doctors;
        this.availability = availability;
    }

    /** Scoped by role: admin sees all, a doctor sees their own, a patient sees their own. */
    @GetMapping @Transactional(readOnly = true)
    public List<Views.AppointmentView> mine(@AuthenticationPrincipal AuthUser me) {
        List<Appointment> list = switch (me.role()) {
            case ADMIN -> appointments.findAllByOrderByAppointmentDateDescAppointmentTimeDesc();
            case DOCTOR -> doctors.findByUserId(me.id())
                    .map(d -> appointments.findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(d.getId()))
                    .orElse(List.of());
            case PATIENT -> appointments.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(me.id());
        };
        return list.stream().map(Views::appointment).toList();
    }

    @PostMapping @PreAuthorize("hasRole('PATIENT')") @Transactional
    public Views.AppointmentView create(@Valid @RequestBody AppointmentRequest r, @AuthenticationPrincipal AuthUser me) {
        if (!LocalDateTime.of(r.appointmentDate(), r.appointmentTime()).isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Appointment must be in the future");
        }
        if (!AvailabilityRules.isOnSlot(r.appointmentTime())) {
            throw new IllegalArgumentException("Appointment start times use 10-minute increments (for example 10:00 or 10:10)");
        }
        // Locking the doctor row makes the "slot already taken" check below safe against simultaneous bookings.
        Doctor doctor = doctors.findByIdForUpdate(r.doctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        // If the doctor published weekly hours, the slot must fall inside them. No hours set means no restriction.
        List<AvailabilityRules.Window> hours = availability.findByDoctorId(doctor.getId()).stream()
                .map(h -> new AvailabilityRules.Window(DayOfWeek.valueOf(h.getDayOfWeek()), h.getStartTime(), h.getEndTime()))
                .toList();
        if (!hours.isEmpty() && !AvailabilityRules.isWithin(hours, r.appointmentDate(), r.appointmentTime())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "The doctor is not available at that time. " + availableSlotsMessage(doctor, r.appointmentDate(), hours));
        }
        if (appointments.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
                doctor.getId(), r.appointmentDate(), r.appointmentTime(), SLOT_FREE)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "That 10-minute start time is already booked. " + availableSlotsMessage(doctor, r.appointmentDate(), hours));
        }
        AppUser patient = users.findById(me.id())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
        Appointment a = new Appointment();
        a.setPatient(patient);
        a.setDoctor(doctor);
        a.setAppointmentDate(r.appointmentDate());
        a.setAppointmentTime(r.appointmentTime());
        a.setReason(r.reason());
        return Views.appointment(appointments.save(a));
    }

    private String availableSlotsMessage(Doctor doctor, java.time.LocalDate date, List<AvailabilityRules.Window> hours) {
        Set<java.time.LocalTime> occupied = appointments.findByDoctorIdAndAppointmentDate(doctor.getId(), date).stream()
                .filter(a -> !SLOT_FREE.contains(a.getStatus()))
                .map(Appointment::getAppointmentTime)
                .collect(java.util.stream.Collectors.toSet());
        List<java.time.LocalTime> available = AvailabilityRules.availableStartTimes(
                date, hours, occupied, LocalDateTime.now());
        String ranges = AvailabilityRules.formatStartTimeRanges(available);
        if (ranges.isEmpty()) {
            return "No other 10-minute start times are available for this doctor on " + date + ".";
        }
        return "Available start-time ranges for " + date + " (every 10 minutes, endpoints included): " + ranges + ".";
    }

    @PatchMapping("/{id}/status") @Transactional
    public Views.AppointmentView status(@PathVariable Long id, @Valid @RequestBody StatusRequest body,
                                        @AuthenticationPrincipal AuthUser me) {
        Appointment a = appointments.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Appointment not found"));
        AppointmentStatus next;
        try {
            next = AppointmentStatus.valueOf(body.status().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown status");
        }

        boolean isPatientOwner = me.role() == Role.PATIENT && a.getPatient().getId().equals(me.id());
        boolean isDoctorOwner = me.role() == Role.DOCTOR && a.getDoctor().getUser().getId().equals(me.id());
        boolean isAdmin = me.role() == Role.ADMIN;
        if (!isPatientOwner && !isDoctorOwner && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot change this appointment");
        }
        if (isPatientOwner && next != AppointmentStatus.CANCELLED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Patients can only cancel their own appointments");
        }
        if (!a.getStatus().canMoveTo(next)) {
            throw new IllegalArgumentException("Cannot change an appointment from " + a.getStatus() + " to " + next);
        }
        a.setStatus(next);
        return Views.appointment(appointments.save(a));
    }
}
