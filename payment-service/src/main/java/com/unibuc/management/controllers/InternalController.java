package com.unibuc.management.controllers;

import com.unibuc.management.dto.internal.InternalPaymentRequest;
import com.unibuc.management.dto.summary.PaymentSummaryDTO;
import com.unibuc.management.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal/payments")
@RequiredArgsConstructor
public class InternalController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentSummaryDTO> create(@RequestBody InternalPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPendingPayment(req.getAppointmentId(), req.getPatientId()));
    }

    @GetMapping("/by-appointment/{appointmentId}")
    public ResponseEntity<PaymentSummaryDTO> getByAppointment(@PathVariable Integer appointmentId) {
        return ResponseEntity.ok(paymentService.getSummaryByAppointment(appointmentId));
    }
}
