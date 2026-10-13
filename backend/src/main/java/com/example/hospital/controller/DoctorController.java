package com.example.hospital.controller;

import com.example.hospital.dto.AvailabilityRequest;
import com.example.hospital.dto.DoctorRequest;
import com.example.hospital.dto.Views;
import com.example.hospital.exception.ApiException;
import com.example.hospital.model.AppUser;
import com.example.hospital.model.Department;
import com.example.hospital.model.Doctor;
import com.example.hospital.model.DoctorAvailability;
import com.example.hospital.model.Role;
import com.example.hospital.repository.DepartmentRepository;
import com.example.hospital.repository.DoctorAvailabilityRepository;
import com.example.hospital.repository.DoctorRepository;
import com.example.hospital.repository.UserRepository;
import com.example.hospital.security.AuthUser;
import com.example.hospital.service.AvailabilityRules;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController @RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorRepository doctors;
    private final UserRepository users;
    private final DepartmentRepository departments;
    private final DoctorAvailabilityRepository availability;

    public DoctorController(DoctorRepository doctors, UserRepository users, DepartmentRepository departments,
                            DoctorAvailabilityRepository availability) {
        this.doctors = doctors;
        this.users = users;
        this.departments = departments;
        this.availability = availability;
    }

    @GetMapping @Transactional(readOnly = true)
    public List<Views.DoctorView> all() {
        return doctors.findAll().stream().map(Views::doctor).toList();
    }

    /** Weekly hours of every doctor, so the UI can show them with one request. */
    @GetMapping("/availability") @Transactional(readOnly = true)
    public List<Views.AvailabilityView> allAvailability() {
        return availability.findAll().stream()
                .map(Views::availability)
                .sorted(Comparator.comparing(Views.AvailabilityView::doctorId)
                        .thenComparing(Views.AvailabilityView::day)
                        .thenComparing(Views.AvailabilityView::start))
                .toList();
    }

    /** A doctor replaces their own weekly hours. */
    @PutMapping("/me/availability") @PreAuthorize("hasRole('DOCTOR')") @Transactional
    public List<Views.AvailabilityView> setMyAvailability(@Valid @RequestBody AvailabilityRequest r,
                                                          @AuthenticationPrincipal AuthUser me) {
        Doctor doctor = doctors.findByUserId(me.id())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No doctor profile is linked to your account"));
        List<AvailabilityRules.Window> windows = r.slots().stream()
                .map(s -> new AvailabilityRules.Window(s.day(), s.start(), s.end()))
                .toList();
        AvailabilityRules.validate(windows);
        availability.deleteByDoctorId(doctor.getId());
        availability.flush();
        List<DoctorAvailability> rows = windows.stream().map(w -> {
            DoctorAvailability a = new DoctorAvailability();
            a.setDoctor(doctor);
            a.setDayOfWeek(w.day().name());
            a.setStartTime(w.start());
            a.setEndTime(w.end());
            return a;
        }).toList();
        return availability.saveAll(rows).stream().map(Views::availability).toList();
    }

    @GetMapping("/{id}") @Transactional(readOnly = true)
    public Views.DoctorView one(@PathVariable Long id) {
        return Views.doctor(doctors.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor not found")));
    }

    @PostMapping @PreAuthorize("hasRole('ADMIN')") @Transactional
    public Views.DoctorView create(@Valid @RequestBody DoctorRequest r) {
        AppUser user = users.findByEmail(r.email().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No registered user has that email. Ask the doctor to register first."));
        if (user.getRole() == Role.ADMIN) throw new IllegalArgumentException("Admin accounts cannot be doctors");
        if (doctors.existsByUserId(user.getId())) throw new IllegalArgumentException("This user already has a doctor profile");
        Department department = null;
        if (r.departmentId() != null) {
            department = departments.findById(r.departmentId()).orElseThrow(() -> new IllegalArgumentException("Department not found"));
        }
        user.setRole(Role.DOCTOR);
        users.save(user);
        Doctor d = new Doctor();
        d.setUser(user);
        d.setSpecialization(r.specialization());
        d.setQualification(r.qualification());
        d.setExperienceYears(r.experienceYears());
        d.setDepartment(department);
        d.setConsultationHours(r.consultationHours());
        return Views.doctor(doctors.save(d));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") @Transactional
    public void delete(@PathVariable Long id) {
        Doctor d = doctors.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Doctor not found"));
        AppUser user = d.getUser();
        availability.deleteByDoctorId(d.getId());
        doctors.delete(d);
        doctors.flush();            // surfaces a foreign-key conflict (doctor has appointments) right here as a 409
        user.setRole(Role.PATIENT);
        users.save(user);
    }
}
