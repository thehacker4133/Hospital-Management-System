package com.example.hospital.repository;

import com.example.hospital.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientIdOrderByIdDesc(Long patientId);
    List<Prescription> findByDoctorIdOrderByIdDesc(Long doctorId);
}
