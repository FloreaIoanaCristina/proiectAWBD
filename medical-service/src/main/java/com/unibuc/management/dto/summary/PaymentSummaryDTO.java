package com.unibuc.management.dto.summary;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryDTO {

    private Long id;
    private String status;
    private BigDecimal amount;
}
