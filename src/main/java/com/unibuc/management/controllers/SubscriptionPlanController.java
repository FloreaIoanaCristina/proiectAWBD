package com.unibuc.management.controllers;

import com.unibuc.management.dto.validation.SubscriptionPlanRequestDTO;
import com.unibuc.management.entities.SubscriptionPlan;
import com.unibuc.management.services.SubscriptionPlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription-plans")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @Autowired
    public SubscriptionPlanController(SubscriptionPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionPlan>> getAllSubscriptionPlans() {
        return ResponseEntity.ok(subscriptionPlanService.getAllSubscriptionPlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlan> getSubscriptionPlanById(@PathVariable Integer id) {
        return ResponseEntity.ok(subscriptionPlanService.getSubscriptionPlanById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<SubscriptionPlan> create(@Valid @RequestBody SubscriptionPlanRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionPlanService.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<SubscriptionPlan> update(@PathVariable Integer id, @Valid @RequestBody SubscriptionPlanRequestDTO dto) {
        return ResponseEntity.ok(subscriptionPlanService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        subscriptionPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

