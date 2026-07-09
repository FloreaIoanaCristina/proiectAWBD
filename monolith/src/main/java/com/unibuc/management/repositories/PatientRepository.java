package com.unibuc.management.repositories;

import com.unibuc.management.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Optional<Patient> findByUserUsername(String username);
    Optional<Patient> findByUserId(Long userId);
}