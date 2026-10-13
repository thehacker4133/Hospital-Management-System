package com.example.hospital.model;
import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
public class DoctorAvailability {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private Doctor doctor;
    @Column(nullable=false) private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    public DoctorAvailability() {}
    public Long getId(){return id;} public Doctor getDoctor(){return doctor;} public void setDoctor(Doctor v){doctor=v;}
    public String getDayOfWeek(){return dayOfWeek;} public void setDayOfWeek(String v){dayOfWeek=v;}
    public LocalTime getStartTime(){return startTime;} public void setStartTime(LocalTime v){startTime=v;}
    public LocalTime getEndTime(){return endTime;} public void setEndTime(LocalTime v){endTime=v;}
}
