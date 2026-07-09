package com.unibuc.management.mappers;

import com.unibuc.management.domain.ServiceCoverage;
import com.unibuc.management.dto.response.ServiceCoverageResponseDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServiceCoverageMapper {

    public static ServiceCoverageResponseDTO toResponseDTO(ServiceCoverage coverage) {

        if (coverage == null) {
            return null;
        }

        return ServiceCoverageResponseDTO.builder()
                .id(coverage.getId())
                .medicalService(
                        MedicalServiceMapper.toSummary(coverage.getMedicalService()))
                .insuranceProvider(
                        InsuranceProviderMapper.toSummary(coverage.getInsuranceProvider()))
                .coveragePercent(coverage.getCoveragePercent())
                .build();
    }
}