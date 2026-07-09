package com.unibuc.management.dto.response;

import com.unibuc.management.dto.summary.AppointmentSummaryDTO;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private Long id;

    private BigDecimal amount;

    private String paymentMethod;

    private LocalDateTime paymentDate;

    private String status;

    private AppointmentSummaryDTO appointment;
}