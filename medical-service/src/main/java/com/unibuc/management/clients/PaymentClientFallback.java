package com.unibuc.management.clients;

import com.unibuc.management.dto.internal.CreatePaymentRequest;
import com.unibuc.management.dto.summary.PaymentSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class PaymentClientFallback implements PaymentClient {

    @Override
    public PaymentSummaryDTO createForAppointment(CreatePaymentRequest request) {
        log.warn("payment-service indisponibil. Plata pentru programarea {} nu a putut fi generată acum.",
                request != null ? request.getAppointmentId() : null);
        return PaymentSummaryDTO.builder().status("UNAVAILABLE").build();
    }

    @Override
    public PaymentSummaryDTO getByAppointment(Integer appointmentId) {
        log.warn("payment-service indisponibil. Nu se poate citi plata pentru programarea {}.", appointmentId);
        return null;
    }
}
