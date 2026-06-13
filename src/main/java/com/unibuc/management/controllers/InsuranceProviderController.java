package com.unibuc.management.controllers;

import com.unibuc.management.dto.validation.InsuranceProviderRequestDTO;
import com.unibuc.management.entities.InsuranceProvider;
import com.unibuc.management.services.InsuranceProviderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insurance-providers")
public class InsuranceProviderController {

    private final InsuranceProviderService insuranceProviderService;

    @Autowired
    public InsuranceProviderController(InsuranceProviderService insuranceProviderService) {
        this.insuranceProviderService = insuranceProviderService;
    }

    @GetMapping
    public ResponseEntity<Page<InsuranceProvider>> getAllInsuranceProviders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {

        String sortBy = sort[0];
        Sort.Direction direction = (sort.length > 1 && sort[1].equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(insuranceProviderService.getAllInsuranceProvidersPaged(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<InsuranceProvider>> getAllUnpaged() {
        return ResponseEntity.ok(insuranceProviderService.getAllInsuranceProviders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsuranceProvider> getInsuranceProviderById(@PathVariable Integer id) {
        return ResponseEntity.ok(insuranceProviderService.getInsuranceProviderById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<InsuranceProvider> createInsuranceProvider(@Valid @RequestBody InsuranceProviderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(insuranceProviderService.createInsuranceProvider(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<InsuranceProvider> updateInsuranceProvider(@PathVariable Integer id,
                                                                     @Valid @RequestBody InsuranceProviderRequestDTO dto) {
        return ResponseEntity.ok(insuranceProviderService.updateInsuranceProvider(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteInsuranceProvider(@PathVariable Integer id) {
        insuranceProviderService.deleteInsuranceProvider(id);
        return ResponseEntity.noContent().build();
    }
}
