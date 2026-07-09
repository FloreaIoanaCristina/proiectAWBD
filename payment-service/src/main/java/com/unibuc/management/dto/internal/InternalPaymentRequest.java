package com.unibuc.management.dto.internal;

import lombok.Data;

@Data
public class InternalPaymentRequest {

    private Integer appointmentId;
    private Integer patientId;
}
