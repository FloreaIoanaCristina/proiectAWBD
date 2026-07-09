package com.unibuc.management.repositories;

import com.unibuc.management.domain.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalServiceRepository extends JpaRepository<MedicalService, Integer> {
    List<MedicalService> findBySpecialization(String specialization);
}