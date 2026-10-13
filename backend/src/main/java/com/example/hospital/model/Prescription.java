package com.example.hospital.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Prescription {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private AppUser patient;
    @ManyToOne(optional=false) private Doctor doctor;
    @ManyToOne private Appointment appointment;
    @Column(nullable=false) private String medicine;
    private String dosage;
    private String frequency;
    private String duration;
    @Column(length=2000) private String instructions;
    private LocalDateTime createdAt = LocalDateTime.now();
    public Prescription() {}
    public Long getId(){return id;} public AppUser getPatient(){return patient;} public void setPatient(AppUser v){patient=v;}
    public Doctor getDoctor(){return doctor;} public void setDoctor(Doctor v){doctor=v;}
    public Appointment getAppointment(){return appointment;} public void setAppointment(Appointment v){appointment=v;}
    public String getMedicine(){return medicine;} public void setMedicine(String v){medicine=v;}
    public String getDosage(){return dosage;} public void setDosage(String v){dosage=v;}
    public String getFrequency(){return frequency;} public void setFrequency(String v){frequency=v;}
    public String getDuration(){return duration;} public void setDuration(String v){duration=v;}
    public String getInstructions(){return instructions;} public void setInstructions(String v){instructions=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
