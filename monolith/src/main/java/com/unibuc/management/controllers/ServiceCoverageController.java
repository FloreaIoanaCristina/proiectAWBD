package com.unibuc.management.controllers;

import com.unibuc.management.dto.request.ServiceCoverageRequestDTO;
import com.unibuc.management.dto.response.ServiceCoverageResponseDTO;
import com.unibuc.management.services.ServiceCoverageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-coverages")
@RequiredArgsConstructor
public class ServiceCoverageController {

    private final ServiceCoverageService serviceCoverageService;

    @GetMapping
    public ResponseEntity<List<ServiceCoverageResponseDTO>> getAll() {
        return ResponseEntity.ok(serviceCoverageService.getAllCoverages());
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ServiceCoverageResponseDTO> create(@Valid @RequestBody ServiceCoverageRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceCoverageService.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ServiceCoverageResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ServiceCoverageRequestDTO dto) {
        return ResponseEntity.ok(serviceCoverageService.updateCoverage(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        serviceCoverageService.deleteCoverage(id);
        return ResponseEntity.noContent().build();
    }
}