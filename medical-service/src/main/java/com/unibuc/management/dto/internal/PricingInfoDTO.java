package com.unibuc.management.dto.internal;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingInfoDTO {

    private Integer patientId;
    private double price;
    private boolean subscription;
    private Integer coveragePercent;
}
