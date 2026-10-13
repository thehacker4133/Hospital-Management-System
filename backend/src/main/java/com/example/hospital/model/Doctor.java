package com.example.hospital.model;
import jakarta.persistence.*;

@Entity
public class Doctor {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(optional=false) private AppUser user;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    @ManyToOne private Department department;
    private String consultationHours;
    private Boolean sample = false;
    public Doctor() {}
    public Long getId(){return id;} public AppUser getUser(){return user;} public void setUser(AppUser v){user=v;}
    public String getSpecialization(){return specialization;} public void setSpecialization(String v){specialization=v;}
    public String getQualification(){return qualification;} public void setQualification(String v){qualification=v;}
    public Integer getExperienceYears(){return experienceYears;} public void setExperienceYears(Integer v){experienceYears=v;}
    public Department getDepartment(){return department;} public void setDepartment(Department v){department=v;}
    public String getConsultationHours(){return consultationHours;} public void setConsultationHours(String v){consultationHours=v;}
    public Boolean getSample(){return sample;} public void setSample(Boolean v){sample=v;}
}
