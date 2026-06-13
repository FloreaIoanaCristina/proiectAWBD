package com.unibuc.management.repositories;

import com.unibuc.management.entities.MedicalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalServiceRepository extends JpaRepository<MedicalService, Integer> {

    List<MedicalService> findBySpecialization(String specialization);

}