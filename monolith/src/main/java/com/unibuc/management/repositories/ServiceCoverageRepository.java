package com.unibuc.management.repositories;

import com.unibuc.management.domain.ServiceCoverage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ServiceCoverageRepository extends JpaRepository<ServiceCoverage, Long> {

    Optional<ServiceCoverage> findByMedicalServiceIdAndInsuranceProviderId(
            Integer medicalServiceId,
            Integer providerId
    );
}