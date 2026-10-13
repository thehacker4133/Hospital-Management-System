package com.example.hospital.model;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class MedicalRecord {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private AppUser patient;
    @ManyToOne(optional=false) private Doctor doctor;
    @OneToOne private Appointment appointment;
    private String symptoms;
    private String diagnosis;
    @Column(length=4000) private String notes;
    private LocalDate recordDate = LocalDate.now();
    public MedicalRecord() {}
    public Long getId(){return id;} public AppUser getPatient(){return patient;} public void setPatient(AppUser v){patient=v;}
    public Doctor getDoctor(){return doctor;} public void setDoctor(Doctor v){doctor=v;}
    public Appointment getAppointment(){return appointment;} public void setAppointment(Appointment v){appointment=v;}
    public String getSymptoms(){return symptoms;} public void setSymptoms(String v){symptoms=v;}
    public String getDiagnosis(){return diagnosis;} public void setDiagnosis(String v){diagnosis=v;}
    public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
    public LocalDate getRecordDate(){return recordDate;} public void setRecordDate(LocalDate v){recordDate=v;}
}
