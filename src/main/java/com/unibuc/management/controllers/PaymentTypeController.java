package com.unibuc.management.controllers;

import com.unibuc.management.dto.validation.PaymentTypeRequestDTO;
import com.unibuc.management.entities.PaymentType;
import com.unibuc.management.services.PaymentTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-types")
public class PaymentTypeController {

    private final PaymentTypeService paymentTypeService;

    public PaymentTypeController(PaymentTypeService paymentTypeService) {
        this.paymentTypeService = paymentTypeService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentType>> getAll() {
        return ResponseEntity.ok(paymentTypeService.getAllPaymentTypes());
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PaymentType> create(@Valid @RequestBody PaymentTypeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentTypeService.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PaymentType> update(@PathVariable Integer id, @Valid @RequestBody PaymentTypeRequestDTO dto) {
        return ResponseEntity.ok(paymentTypeService.updatePaymentType(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        paymentTypeService.deletePaymentType(id);
        return ResponseEntity.noContent().build();
    }
}