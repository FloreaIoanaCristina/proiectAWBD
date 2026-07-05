package com.unibuc.management.dto.response;

import com.unibuc.management.dto.summary.MedicalServiceSummaryDTO;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponseDTO {

    private Integer id;
    private String name;
    private String office;
    private Integer numberOfPTOdays;

    private MedicalServiceSummaryDTO medicalService;

    private Long userId;
}
