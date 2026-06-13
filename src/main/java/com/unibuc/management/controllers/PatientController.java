package com.unibuc.management.controllers;

import com.unibuc.management.dto.validation.PatientRequestDTO;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.services.PatientService;
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
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<Patient>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {

        String sortBy = sort[0];
        Sort.Direction direction = (sort.length > 1 && sort[1].equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(patientService.getAllPatientsPaged(pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Patient>> getAllPatientsUnpaged() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Patient> getPatientById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody PatientRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.createPatient(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Patient> updatePatient(@PathVariable("id") Integer id, @Valid @RequestBody PatientRequestDTO dto) {
        return ResponseEntity.ok(patientService.updatePatient(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Void> deletePatient(@PathVariable("id") Integer id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}