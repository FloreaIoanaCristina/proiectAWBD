package com.unibuc.management.controllers;

import com.unibuc.management.dto.validation.MedicalServiceRequestDTO;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.services.MedicalServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medical-services")
public class MedicalServiceController {

    private final MedicalServiceService medicalServiceService;

    public MedicalServiceController(MedicalServiceService medicalServiceService) {
        this.medicalServiceService = medicalServiceService;
    }

    @GetMapping
    public ResponseEntity<List<MedicalService>> getAllMedicalServices() {
        return ResponseEntity.ok(medicalServiceService.getAllServices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalService> getMedicalServiceById(@PathVariable Integer id) {
        return ResponseEntity.ok(medicalServiceService.getMedicalServiceById(id));
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<MedicalService>> getBySpecialization(@PathVariable String specialization) {
        return ResponseEntity.ok(medicalServiceService.getServicesBySpecialization(specialization));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalService> createService(@Valid @RequestBody MedicalServiceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalServiceService.saveFromDto(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalService> updateService(@PathVariable Integer id,
                                                        @Valid @RequestBody MedicalServiceRequestDTO dto) {
        return ResponseEntity.ok(medicalServiceService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteService(@PathVariable Integer id) {
        medicalServiceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
