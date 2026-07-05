package com.unibuc.management.mappers;

import com.unibuc.management.domain.Payment;
import com.unibuc.management.dto.response.PaymentResponseDTO;
import com.unibuc.management.dto.summary.PaymentSummaryDTO;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

@UtilityClass
public class PaymentMapper {

    public static PaymentResponseDTO toResponseDTO(Payment payment) {

        if (payment == null) {
            return null;
        }

        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .appointmentId(
                        payment.getAppointment() != null
                                ? payment.getAppointment().getId()
                                : null
                )
                .build();
    }

    public static PaymentSummaryDTO toSummary(Payment payment) {

        if (payment == null) {
            return null;
        }

        return PaymentSummaryDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .build();
    }
}