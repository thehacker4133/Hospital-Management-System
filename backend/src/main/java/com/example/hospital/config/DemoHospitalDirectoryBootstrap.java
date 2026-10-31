package com.example.hospital.config;

import com.example.hospital.model.AppUser;
import com.example.hospital.model.Department;
import com.example.hospital.model.Doctor;
import com.example.hospital.model.DoctorAvailability;
import com.example.hospital.model.Role;
import com.example.hospital.repository.DepartmentRepository;
import com.example.hospital.repository.DoctorAvailabilityRepository;
import com.example.hospital.repository.DoctorRepository;
import com.example.hospital.repository.UserRepository;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads fictional directory entries for the Replit preview only.
 * Sample users have random passwords and are not real, sign-in-ready clinicians.
 */
@Component
@Profile({"replit", "local-demo"})
public class DemoHospitalDirectoryBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoHospitalDirectoryBootstrap.class);
    private static final List<DayOfWeek> CLINIC_DAYS = List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    private static final LocalTime CLINIC_START = LocalTime.of(9, 0);
    private static final LocalTime CLINIC_END = LocalTime.of(17, 0);

    private static final List<DepartmentSeed> DIRECTORY = List.of(
            department("General Medicine", "Primary care, routine checkups, and common illnesses.",
                    doctor("Asha Mehta", "General Physician", "MBBS, MD (Internal Medicine)", 12),
                    doctor("Daniel Brooks", "Internal Medicine", "MBBS, MD (General Medicine)", 9)),
            department("Cardiology", "Heart and cardiovascular care.",
                    doctor("Rohan Iyer", "Cardiologist", "MBBS, MD, DM (Cardiology)", 14),
                    doctor("Leena Shah", "Interventional Cardiologist", "MBBS, MD (Cardiology)", 10)),
            department("Pediatrics", "Medical care for infants, children, and adolescents.",
                    doctor("Neha Kapoor", "Pediatrician", "MBBS, MD (Pediatrics)", 11),
                    doctor("Arjun Desai", "Pediatrician", "MBBS, DCH", 8)),
            department("Orthopedics", "Bone, joint, and musculoskeletal care.",
                    doctor("Maya Nair", "Orthopedic Surgeon", "MBBS, MS (Orthopedics)", 13),
                    doctor("Ethan Cole", "Sports Medicine Specialist", "MBBS, MS (Orthopedics)", 9)),
            department("Obstetrics & Gynecology", "Pregnancy, reproductive health, and women's care.",
                    doctor("Priya Menon", "Obstetrician & Gynecologist", "MBBS, MS (Obstetrics & Gynecology)", 15),
                    doctor("Sofia Reyes", "Gynecologist", "MBBS, DGO", 10)),
            department("Dermatology", "Skin, hair, and nail health.",
                    doctor("Kavya Rao", "Dermatologist", "MBBS, MD (Dermatology)", 9),
                    doctor("Noah Bennett", "Dermatologist", "MBBS, MD (Dermatology)", 7)),
            department("Neurology", "Brain, spine, and nervous system care.",
                    doctor("Vikram Sethi", "Neurologist", "MBBS, MD, DM (Neurology)", 16),
                    doctor("Grace Kim", "Neurologist", "MBBS, MD (Neurology)", 11)),
            department("ENT & Head and Neck", "Ear, nose, throat, and related head and neck care.",
                    doctor("Ishita Bose", "ENT Specialist", "MBBS, MS (Otolaryngology)", 12),
                    doctor("Oliver Wright", "ENT Surgeon", "MBBS, DLO", 8)),
            department("Ophthalmology", "Eye examinations and vision care.",
                    doctor("Ananya Pillai", "Ophthalmologist", "MBBS, MS (Ophthalmology)", 13),
                    doctor("Lucas Martin", "Ophthalmologist", "MBBS, DO", 9)),
            department("Gastroenterology", "Digestive system and liver care.",
                    doctor("Kabir Khanna", "Gastroenterologist", "MBBS, MD, DM (Gastroenterology)", 14),
                    doctor("Emma Foster", "Gastroenterologist", "MBBS, MD (Gastroenterology)", 8)),
            department("Pulmonology", "Lung and respiratory care.",
                    doctor("Sana Qureshi", "Pulmonologist", "MBBS, MD (Pulmonary Medicine)", 10),
                    doctor("Henry Davis", "Respiratory Physician", "MBBS, MD (Pulmonology)", 7)),
            department("Nephrology", "Kidney health and related conditions.",
                    doctor("Aditya Kulkarni", "Nephrologist", "MBBS, MD, DM (Nephrology)", 12),
                    doctor("Mia Thompson", "Nephrologist", "MBBS, MD (Nephrology)", 9)),
            department("Endocrinology", "Hormone, thyroid, and metabolic health.",
                    doctor("Nisha Verma", "Endocrinologist", "MBBS, MD, DM (Endocrinology)", 11),
                    doctor("James Parker", "Endocrinologist", "MBBS, MD (Endocrinology)", 8)),
            department("Oncology", "Cancer evaluation and coordinated treatment.",
                    doctor("Rahul Bhat", "Medical Oncologist", "MBBS, MD (Medical Oncology)", 15),
                    doctor("Chloe Adams", "Surgical Oncologist", "MBBS, MS (Surgical Oncology)", 10)),
            department("Urology", "Urinary tract and men's health care.",
                    doctor("Dev Patel", "Urologist", "MBBS, MS, MCh (Urology)", 13),
                    doctor("Isabella Cruz", "Urologist", "MBBS, MS (Urology)", 9)),
            department("Psychiatry & Behavioral Health", "Mental health assessment and treatment.",
                    doctor("Meera Joshi", "Psychiatrist", "MBBS, MD (Psychiatry)", 12),
                    doctor("Samuel Reed", "Psychiatrist", "MBBS, MD (Psychiatry)", 8)),
            department("Emergency Medicine", "Urgent assessment and emergency stabilization.",
                    doctor("Karan Malhotra", "Emergency Physician", "MBBS, MD (Emergency Medicine)", 10),
                    doctor("Amelia Scott", "Emergency Physician", "MBBS, MD (Emergency Medicine)", 7)),
            department("General & Laparoscopic Surgery", "General surgical consultation and care.",
                    doctor("Siddharth Rao", "General Surgeon", "MBBS, MS (General Surgery)", 14),
                    doctor("Ella Morgan", "Laparoscopic Surgeon", "MBBS, MS (General Surgery)", 10)),
            department("Neurosurgery", "Surgical care for the brain, spine, and nervous system.",
                    doctor("Nikhil Anand", "Neurosurgeon", "MBBS, MS, MCh (Neurosurgery)", 17),
                    doctor("Zoe Turner", "Neurosurgeon", "MBBS, MS (Neurosurgery)", 11)),
            department("Anesthesiology & Critical Care", "Anesthesia and intensive care support.",
                    doctor("Pooja Reddy", "Anesthesiologist", "MBBS, MD (Anesthesiology)", 12),
                    doctor("Jack Wilson", "Critical Care Specialist", "MBBS, MD (Anesthesiology)", 9)),
            department("Radiology & Imaging", "Diagnostic imaging and image-guided care.",
                    doctor("Sonal Gupta", "Radiologist", "MBBS, MD (Radiodiagnosis)", 11),
                    doctor("Benjamin Lee", "Radiologist", "MBBS, MD (Radiology)", 8)),
            department("Pathology & Laboratory Medicine", "Laboratory testing and diagnostic pathology.",
                    doctor("Tara Sood", "Pathologist", "MBBS, MD (Pathology)", 10),
                    doctor("David Clark", "Laboratory Medicine Specialist", "MBBS, MD (Pathology)", 7)),
            department("Dentistry & Oral Surgery", "Dental, oral, and maxillofacial care.",
                    doctor("Ira Fernandes", "Dentist", "BDS, MDS (Oral Surgery)", 12),
                    doctor("Matthew Green", "Dentist", "BDS, MDS (Orthodontics)", 8)),
            department("Physical Therapy & Rehabilitation", "Movement, recovery, and rehabilitation care.",
                    doctor("Ritu Sharma", "Physiotherapist", "BPT, MPT (Rehabilitation)", 9),
                    doctor("Liam Harris", "Rehabilitation Specialist", "BPT, MPT (Sports Therapy)", 7)),
            department("Rheumatology", "Joint, autoimmune, and inflammatory conditions.",
                    doctor("Anil Prasad", "Rheumatologist", "MBBS, MD, DM (Rheumatology)", 13),
                    doctor("Natalie Young", "Rheumatologist", "MBBS, MD (Rheumatology)", 8)),
            department("Infectious Disease", "Complex and persistent infections.",
                    doctor("Farah Ali", "Infectious Disease Specialist", "MBBS, MD (Infectious Disease)", 11),
                    doctor("Ryan Evans", "Infectious Disease Specialist", "MBBS, MD (Internal Medicine)", 8)),
            department("Plastic & Reconstructive Surgery", "Reconstructive and plastic surgical consultation.",
                    doctor("Aarav Malhotra", "Plastic Surgeon", "MBBS, MS, MCh (Plastic Surgery)", 14),
                    doctor("Victoria Hall", "Reconstructive Surgeon", "MBBS, MS (Plastic Surgery)", 9)),
            department("Nutrition & Dietetics", "Nutrition guidance for health and recovery.",
                    doctor("Saira Khan", "Clinical Nutritionist", "MSc (Clinical Nutrition)", 8),
                    doctor("Thomas Baker", "Dietitian", "MSc (Dietetics)", 6))
    );

    private final DepartmentRepository departments;
    private final UserRepository users;
    private final DoctorRepository doctors;
    private final DoctorAvailabilityRepository availability;
    private final PasswordEncoder passwordEncoder;

    public DemoHospitalDirectoryBootstrap(
            DepartmentRepository departments,
            UserRepository users,
            DoctorRepository doctors,
            DoctorAvailabilityRepository availability,
            PasswordEncoder passwordEncoder) {
        this.departments = departments;
        this.users = users;
        this.doctors = doctors;
        this.availability = availability;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int profileCount = 0;
        for (DepartmentSeed departmentSeed : DIRECTORY) {
            Department department = departments.findByNameIgnoreCase(departmentSeed.name())
                    .orElseGet(() -> departments.save(
                            new Department(departmentSeed.name(), departmentSeed.description())));
            for (int i = 0; i < departmentSeed.doctors().size(); i++) {
                if (ensureDoctor(department, departmentSeed, departmentSeed.doctors().get(i), i + 1)) {
                    profileCount++;
                }
            }
        }
        log.info("Replit demo directory ready: {} departments, {} sample doctors",
                DIRECTORY.size(), profileCount);
    }

    private boolean ensureDoctor(Department department, DepartmentSeed departmentSeed, DoctorSeed seed, int index) {
        String email = demoEmail(departmentSeed.name(), index);
        AppUser user = users.findByEmail(email).orElse(null);
        if (user != null) {
            Doctor existingDoctor = doctors.findByUserId(user.getId()).orElse(null);
            if (existingDoctor == null || !Boolean.TRUE.equals(existingDoctor.getSample())) return false;
            ensureAvailability(existingDoctor);
            return true;
        }

        user = users.save(new AppUser(
                seed.name(), email, passwordEncoder.encode(UUID.randomUUID().toString()), Role.DOCTOR, null));
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialization(seed.specialization());
        doctor.setQualification(seed.qualification());
        doctor.setExperienceYears(seed.experienceYears());
        doctor.setDepartment(department);
        doctor.setConsultationHours("Monday–Friday, 9:00 AM–5:00 PM");
        doctor.setSample(true);
        ensureAvailability(doctors.save(doctor));
        return true;
    }

    private void ensureAvailability(Doctor doctor) {
        if (!availability.findByDoctorId(doctor.getId()).isEmpty()) return;
        List<DoctorAvailability> hours = CLINIC_DAYS.stream().map(day -> {
            DoctorAvailability row = new DoctorAvailability();
            row.setDoctor(doctor);
            row.setDayOfWeek(day.name());
            row.setStartTime(CLINIC_START);
            row.setEndTime(CLINIC_END);
            return row;
        }).toList();
        availability.saveAll(hours);
    }

    private static String demoEmail(String department, int index) {
        String slug = department.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", ".")
                .replaceAll("^\\.|\\.$", "");
        return "sample." + slug + "." + index + "@carepoint.example";
    }

    private static DepartmentSeed department(String name, String description, DoctorSeed... doctors) {
        return new DepartmentSeed(name, description, List.of(doctors));
    }

    private static DoctorSeed doctor(String name, String specialization, String qualification, int years) {
        return new DoctorSeed(name, specialization, qualification, years);
    }

    private record DepartmentSeed(String name, String description, List<DoctorSeed> doctors) {}
    private record DoctorSeed(String name, String specialization, String qualification, int experienceYears) {}
}
