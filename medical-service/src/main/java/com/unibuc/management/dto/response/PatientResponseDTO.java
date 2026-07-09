package com.unibuc.management.dto.response;

import com.unibuc.management.dto.summary.InsuranceProviderSummaryDTO;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponseDTO {

    private Integer id;

    private String name;

    private String medicalRecord;

    private Boolean subscription;

    private LocalDate age;

    private Boolean sex;

    private InsuranceProviderSummaryDTO insuranceProvider;

    private Long userId;
}