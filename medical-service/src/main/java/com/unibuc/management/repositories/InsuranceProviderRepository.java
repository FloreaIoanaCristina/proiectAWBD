package com.unibuc.management.repositories;

import com.unibuc.management.domain.InsuranceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InsuranceProviderRepository extends JpaRepository<InsuranceProvider, Integer> {
}
