package com.example.hospital.controller;

import com.example.hospital.dto.PrescriptionRequest;
import com.example.hospital.dto.RecordRequest;
import com.example.hospital.dto.Views;
import com.example.hospital.exception.ApiException;
import com.example.hospital.model.*;
import com.example.hospital.repository.*;
import com.example.hospital.security.AuthUser;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Clinical data. Patients read only their own; doctors read and write only what they authored, and only for
 * patients they actually have a confirmed or completed appointment with. Admins have no access.
 * Every read and write leaves a line in the "AUDIT" log.
 */
@RestController @RequestMapping("/api")
public class RecordController {
    private static final Logger audit = LoggerFactory.getLogger("AUDIT");
    private static final Set<AppointmentStatus> CARE_STATUSES = Set.of(AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED);

    private final MedicalRecordRepository records;
    private final PrescriptionRepository prescriptions;
    private final UserRepository users;
    private final DoctorRepository doctors;
    private final AppointmentRepository appointments;

    public RecordController(MedicalRecordRepository records, PrescriptionRepository prescriptions, UserRepository users,
                            DoctorRepository doctors, AppointmentRepository appointments) {
        this.records = records;
        this.prescriptions = prescriptions;
        this.users = users;
        this.doctors = doctors;
        this.appointments = appointments;
    }

    // ---------- Medical records ----------

    @GetMapping("/medical-records") @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')") @Transactional(readOnly = true)
    public List<Views.RecordView> myRecords(@AuthenticationPrincipal AuthUser me) {
        audit.info("user={} role={} action=READ_MEDICAL_RECORDS", me.id(), me.role());
        List<MedicalRecord> list = me.role() == Role.PATIENT
                ? records.findByPatientIdOrderByIdDesc(me.id())
                : doctors.findByUserId(me.id()).map(d -> records.findByDoctorIdOrderByIdDesc(d.getId())).orElse(List.of());
        return list.stream().map(Views::medicalRecord).toList();
    }

    @PostMapping("/medical-records") @PreAuthorize("hasRole('DOCTOR')") @Transactional
    public Views.RecordView addRecord(@Valid @RequestBody RecordRequest r, @AuthenticationPrincipal AuthUser me) {
        Doctor doctor = doctorProfile(me);
        AppUser patient = patientOfDoctor(doctor, r.patientId());
        Appointment appointment = linkedAppointment(r.appointmentId(), doctor, patient);
        if (appointment != null && records.existsByAppointmentId(appointment.getId())) {
            throw new IllegalArgumentException("A medical record already exists for this appointment");
        }
        MedicalRecord rec = new MedicalRecord();
        rec.setPatient(patient);
        rec.setDoctor(doctor);
        rec.setAppointment(appointment);
        rec.setSymptoms(r.symptoms());
        rec.setDiagnosis(r.diagnosis());
        rec.setNotes(r.notes());
        MedicalRecord saved = records.save(rec);
        audit.info("user={} role={} action=CREATE_MEDICAL_RECORD record={} patient={}", me.id(), me.role(), saved.getId(), patient.getId());
        return Views.medicalRecord(saved);
    }

    // ---------- Prescriptions ----------

    @GetMapping("/prescriptions") @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')") @Transactional(readOnly = true)
    public List<Views.PrescriptionView> myPrescriptions(@AuthenticationPrincipal AuthUser me) {
        audit.info("user={} role={} action=READ_PRESCRIPTIONS", me.id(), me.role());
        List<Prescription> list = me.role() == Role.PATIENT
                ? prescriptions.findByPatientIdOrderByIdDesc(me.id())
                : doctors.findByUserId(me.id()).map(d -> prescriptions.findByDoctorIdOrderByIdDesc(d.getId())).orElse(List.of());
        return list.stream().map(Views::prescription).toList();
    }

    @PostMapping("/prescriptions") @PreAuthorize("hasRole('DOCTOR')") @Transactional
    public Views.PrescriptionView addPrescription(@Valid @RequestBody PrescriptionRequest r, @AuthenticationPrincipal AuthUser me) {
        Doctor doctor = doctorProfile(me);
        AppUser patient = patientOfDoctor(doctor, r.patientId());
        Appointment appointment = linkedAppointment(r.appointmentId(), doctor, patient);
        Prescription p = new Prescription();
        p.setPatient(patient);
        p.setDoctor(doctor);
        p.setAppointment(appointment);
        p.setMedicine(r.medicine().trim());
        p.setDosage(r.dosage());
        p.setFrequency(r.frequency());
        p.setDuration(r.duration());
        p.setInstructions(r.instructions());
        Prescription saved = prescriptions.save(p);
        audit.info("user={} role={} action=CREATE_PRESCRIPTION prescription={} patient={}", me.id(), me.role(), saved.getId(), patient.getId());
        return Views.prescription(saved);
    }

    // ---------- helpers ----------

    private Doctor doctorProfile(AuthUser me) {
        return doctors.findByUserId(me.id())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No doctor profile is linked to your account"));
    }

    private AppUser patientOfDoctor(Doctor doctor, Long patientId) {
        if (!appointments.existsByDoctorIdAndPatientIdAndStatusIn(doctor.getId(), patientId, CARE_STATUSES)) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "You can only add records for patients who have a confirmed or completed appointment with you");
        }
        return users.findById(patientId).orElseThrow(() -> new IllegalArgumentException("Patient not found"));
    }

    private Appointment linkedAppointment(Long appointmentId, Doctor doctor, AppUser patient) {
        if (appointmentId == null) return null;
        Appointment a = appointments.findById(appointmentId).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        if (!a.getDoctor().getId().equals(doctor.getId()) || !a.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("That appointment does not belong to this doctor and patient");
        }
        return a;
    }
}
