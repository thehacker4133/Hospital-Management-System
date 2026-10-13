package com.example.hospital.controller;

import com.example.hospital.dto.DepartmentRequest;
import com.example.hospital.dto.Views;
import com.example.hospital.exception.ApiException;
import com.example.hospital.model.Department;
import com.example.hospital.repository.DepartmentRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentRepository repo;
    public DepartmentController(DepartmentRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Views.DepartmentView> all() {
        return repo.findAll().stream().map(Views::department).toList();
    }

    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public Views.DepartmentView create(@Valid @RequestBody DepartmentRequest r) {
        String name = r.name().trim();
        if (repo.findByNameIgnoreCase(name).isPresent()) throw new IllegalArgumentException("A department with that name already exists");
        return Views.department(repo.save(new Department(name, r.description())));
    }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public Views.DepartmentView update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest r) {
        Department d = repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Department not found"));
        String name = r.name().trim();
        repo.findByNameIgnoreCase(name).ifPresent(other -> {
            if (!other.getId().equals(id)) throw new IllegalArgumentException("A department with that name already exists");
        });
        d.setName(name);
        d.setDescription(r.description());
        return Views.department(repo.save(d));
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new ApiException(HttpStatus.NOT_FOUND, "Department not found");
        repo.deleteById(id);   // a department still used by doctors fails with 409 via the exception handler
    }
}
