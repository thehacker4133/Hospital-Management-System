package com.example.hospital.dto;

import com.example.hospital.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Response shapes. Entities are never returned directly, so fields such as password hashes,
 * emails and phone numbers cannot leak through nested objects.
 */
public final class Views {
    private Views() {}

    public record UserView(Long id, String name) {}
    public record DepartmentView(Long id, String name, String description) {}
    public record DoctorView(Long id, UserView user, String specialization, String qualification,
                             Integer experienceYears, DepartmentView department, String consultationHours, boolean sample) {}
    public record AppointmentView(Long id, UserView patient, DoctorView doctor, LocalDate appointmentDate,
                                  LocalTime appointmentTime, String reason, AppointmentStatus status) {}
    public record AvailabilityView(Long doctorId, DayOfWeek day, LocalTime start, LocalTime end) {}
    public record RecordView(Long id, UserView patient, DoctorView doctor, Long appointmentId,
                             String symptoms, String diagnosis, String notes, LocalDate recordDate) {}
    public record PrescriptionView(Long id, UserView patient, DoctorView doctor, Long appointmentId,
                                   String medicine, String dosage, String frequency, String duration,
                                   String instructions, LocalDateTime createdAt) {}

    public static UserView user(AppUser u) {
        return u == null ? null : new UserView(u.getId(), u.getName());
    }

    public static DepartmentView department(Department d) {
        return d == null ? null : new DepartmentView(d.getId(), d.getName(), d.getDescription());
    }

    public static DoctorView doctor(Doctor d) {
        return d == null ? null : new DoctorView(d.getId(), user(d.getUser()), d.getSpecialization(),
                d.getQualification(), d.getExperienceYears(), department(d.getDepartment()), d.getConsultationHours(),
                Boolean.TRUE.equals(d.getSample()));
    }

    public static AvailabilityView availability(DoctorAvailability a) {
        return new AvailabilityView(a.getDoctor().getId(), DayOfWeek.valueOf(a.getDayOfWeek()), a.getStartTime(), a.getEndTime());
    }

    public static AppointmentView appointment(Appointment a) {
        return new AppointmentView(a.getId(), user(a.getPatient()), doctor(a.getDoctor()), a.getAppointmentDate(),
                a.getAppointmentTime(), a.getReason(), a.getStatus());
    }

    public static RecordView medicalRecord(MedicalRecord r) {
        return new RecordView(r.getId(), user(r.getPatient()), doctor(r.getDoctor()),
                r.getAppointment() == null ? null : r.getAppointment().getId(),
                r.getSymptoms(), r.getDiagnosis(), r.getNotes(), r.getRecordDate());
    }

    public static PrescriptionView prescription(Prescription p) {
        return new PrescriptionView(p.getId(), user(p.getPatient()), doctor(p.getDoctor()),
                p.getAppointment() == null ? null : p.getAppointment().getId(),
                p.getMedicine(), p.getDosage(), p.getFrequency(), p.getDuration(), p.getInstructions(), p.getCreatedAt());
    }
}
