package com.unibuc.management.dto.internal;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    private Integer appointmentId;
    private Integer patientId;
}
