package com.unibuc.management.repositories;

import com.unibuc.management.entities.ServiceCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceCoverageRepository extends JpaRepository<ServiceCoverage, Long> {

    Optional<ServiceCoverage> findByMedicalServiceIdAndInsuranceProviderId(
            Integer medicalServiceId,
            Integer providerId
    );
}