package com.unibuc.management.dto.summary;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalServiceSummaryDTO {

    private Integer id;
    private String name;
    private String specialization;
    private Double price;
}
