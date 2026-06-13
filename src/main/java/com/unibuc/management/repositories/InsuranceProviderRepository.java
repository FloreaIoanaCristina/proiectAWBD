package com.unibuc.management.repositories;

import com.unibuc.management.entities.InsuranceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsuranceProviderRepository extends JpaRepository<InsuranceProvider, Integer> {
}

