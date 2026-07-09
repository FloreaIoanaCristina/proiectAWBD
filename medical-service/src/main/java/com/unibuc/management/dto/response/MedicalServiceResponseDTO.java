package com.unibuc.management.dto.response;

import com.unibuc.management.dto.summary.DoctorSummaryDTO;
import com.unibuc.management.dto.summary.InsuranceProviderSummaryDTO;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalServiceResponseDTO {

    private Integer id;

    private String name;

    private String specialization;

    private Integer startHour;

    private Integer endHour;

    private Double price;

    private Double rating;

    private Integer nrOfRatings;

    private Set<DoctorSummaryDTO> medicalServiceDoctors;
    private Set<InsuranceProviderSummaryDTO> coveredByInsurances;
}