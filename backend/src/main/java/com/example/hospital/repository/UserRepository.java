package com.example.hospital.repository;
import com.example.hospital.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<AppUser, Long> { Optional<AppUser> findByEmail(String email); boolean existsByEmail(String email); }
