package com.unibuc.management.mappers;

import com.unibuc.management.domain.InsuranceProvider;
import com.unibuc.management.dto.response.InsuranceProviderResponseDTO;
import com.unibuc.management.dto.summary.InsuranceProviderSummaryDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class InsuranceProviderMapper {

    public static InsuranceProviderResponseDTO toResponseDTO(
            InsuranceProvider insuranceProvider) {

        if (insuranceProvider == null) {
            return null;
        }

        return InsuranceProviderResponseDTO.builder()
                .id(insuranceProvider.getId())
                .name(insuranceProvider.getName())
                .contactNumber(insuranceProvider.getContactNumber())
                .build();
    }

    public static InsuranceProviderSummaryDTO toSummary(InsuranceProvider provider) {

        if (provider == null) {
            return null;
        }

        return InsuranceProviderSummaryDTO.builder()
                .id(provider.getId())
                .name(provider.getName())
                .build();
    }
}
