package com.unibuc.management.controllers;

import com.unibuc.management.dto.request.MedicalServiceRequestDTO;
import com.unibuc.management.domain.MedicalService;
import com.unibuc.management.dto.response.MedicalServiceResponseDTO;
import com.unibuc.management.mappers.MedicalServiceMapper;
import com.unibuc.management.services.MedicalServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/medical-services")
@RequiredArgsConstructor
public class MedicalServiceController {

    private final MedicalServiceService medicalServiceService;

    @GetMapping
    public ResponseEntity<List<MedicalServiceResponseDTO>> getAllMedicalServices() {
        return ResponseEntity.ok(medicalServiceService.getAllServices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicalServiceResponseDTO> getMedicalServiceById(@PathVariable Integer id) {
        MedicalService medicalService = medicalServiceService.getMedicalServiceById(id);
        return ResponseEntity.ok(MedicalServiceMapper.toResponseDTO(medicalService));
    }

    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<MedicalServiceResponseDTO>> getBySpecialization(@PathVariable String specialization) {
        return ResponseEntity.ok(medicalServiceService.getServicesBySpecialization(specialization));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalServiceResponseDTO> createService(@Valid @RequestBody MedicalServiceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalServiceService.saveFromDto(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalServiceResponseDTO> updateService(@PathVariable Integer id,
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
