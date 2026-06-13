package com.unibuc.management.repositories;
import com.unibuc.management.entities.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    Optional<Doctor> findByMedicalServiceId(Integer medicalServiceId);
    Optional<Doctor> findByUserUsername(String username);

    Page<Doctor> findAll(Pageable pageable);
}
