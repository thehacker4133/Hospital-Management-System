package com.example.hospital.repository;

import com.example.hospital.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientIdOrderByIdDesc(Long patientId);
    List<MedicalRecord> findByDoctorIdOrderByIdDesc(Long doctorId);
    boolean existsByAppointmentId(Long appointmentId);
}
