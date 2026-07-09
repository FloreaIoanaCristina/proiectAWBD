package com.unibuc.management.clients;

import com.unibuc.management.dto.internal.CreatePaymentRequest;
import com.unibuc.management.dto.summary.PaymentSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", path = "/api/internal/payments", fallback = PaymentClientFallback.class)
public interface PaymentClient {

    @PostMapping
    PaymentSummaryDTO createForAppointment(@RequestBody CreatePaymentRequest request);

    @GetMapping("/by-appointment/{appointmentId}")
    PaymentSummaryDTO getByAppointment(@PathVariable("appointmentId") Integer appointmentId);
}
