package com.unibuc.management.dto.response;

import com.unibuc.management.dto.summary.InsuranceProviderSummaryDTO;
import com.unibuc.management.dto.summary.MedicalServiceSummaryDTO;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCoverageResponseDTO {

    private Long id;

    private MedicalServiceSummaryDTO medicalService;

    private InsuranceProviderSummaryDTO insuranceProvider;

    private Integer coveragePercent;
}