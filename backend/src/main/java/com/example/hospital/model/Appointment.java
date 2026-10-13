package com.example.hospital.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
public class Appointment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private AppUser patient;
    @ManyToOne(optional=false) private Doctor doctor;
    @Column(nullable=false) private LocalDate appointmentDate;
    @Column(nullable=false) private LocalTime appointmentTime;
    private String reason;
    @Enumerated(EnumType.STRING) private AppointmentStatus status = AppointmentStatus.PENDING;
    private LocalDateTime createdAt = LocalDateTime.now();
    public Appointment() {}
    public Long getId(){return id;} public AppUser getPatient(){return patient;} public void setPatient(AppUser v){patient=v;}
    public Doctor getDoctor(){return doctor;} public void setDoctor(Doctor v){doctor=v;}
    public LocalDate getAppointmentDate(){return appointmentDate;} public void setAppointmentDate(LocalDate v){appointmentDate=v;}
    public LocalTime getAppointmentTime(){return appointmentTime;} public void setAppointmentTime(LocalTime v){appointmentTime=v;}
    public String getReason(){return reason;} public void setReason(String v){reason=v;}
    public AppointmentStatus getStatus(){return status;} public void setStatus(AppointmentStatus v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
