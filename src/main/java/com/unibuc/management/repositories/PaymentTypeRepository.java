package com.unibuc.management.repositories;

import com.unibuc.management.entities.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface PaymentTypeRepository extends JpaRepository<PaymentType, Integer> {
    Optional<PaymentType> findByMedicalServiceIdAndWithInsuranceAndWithSubscription(
            Integer medicalServiceId, boolean withInsurance, boolean withSubscription);
}