package com.example.hospital.repository;

import com.example.hospital.model.Appointment;
import com.example.hospital.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(Long patientId);
    List<Appointment> findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(Long doctorId);
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);
    List<Appointment> findAllByOrderByAppointmentDateDescAppointmentTimeDesc();

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNotIn(
            Long doctorId, LocalDate date, LocalTime time, Collection<AppointmentStatus> statuses);

    boolean existsByDoctorIdAndPatientIdAndStatusIn(Long doctorId, Long patientId, Collection<AppointmentStatus> statuses);
}
