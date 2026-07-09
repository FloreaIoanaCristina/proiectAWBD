package com.unibuc.management.controllers;

import com.unibuc.management.dto.request.AppointmentRequestDTO;
import com.unibuc.management.dto.response.AppointmentResponseDTO;
import com.unibuc.management.services.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<AppointmentResponseDTO> create(@Valid @RequestBody AppointmentRequestDTO dto, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(authentication, dto));
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<OffsetDateTime>> getAvailableTimeSlots(
            @RequestParam Integer medicalServiceId,
            @RequestParam(required = false) Integer doctorId,
            @RequestParam String date) {

        return ResponseEntity.ok(
                appointmentService.getAvailableTimeSlots(
                        medicalServiceId,
                        doctorId,
                        date)
        );
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
    public ResponseEntity<Page<AppointmentResponseDTO>> getAppointmentsByPatientId(
            @PathVariable Integer patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentFrom,asc") String[] sort,
            Authentication authentication) {

        String sortBy = sort[0];
        Sort.Direction direction = (sort.length > 1 && sort[1].equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(
                appointmentService.getAppointmentsByPatientId(
                        patientId,
                        pageable,
                        authentication)
        );
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<AppointmentResponseDTO>> getAppointmentsByDoctorId(
            @PathVariable Integer doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentFrom,asc") String[] sort,
            Authentication authentication) {

        String sortBy = sort[0];
        Sort.Direction direction = (sort.length > 1 && sort[1].equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDoctorId(
                        doctorId,
                        pageable,
                        authentication)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(
            @PathVariable Integer id,
            @RequestParam
            @NotNull(message = "Noua dată este obligatorie.")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime appointmentFrom,
            Authentication authentication) {

        AppointmentResponseDTO updatedAppointment =
                appointmentService.updateAppointment(
                        id,
                        appointmentFrom,
                        authentication);

        return ResponseEntity.ok(updatedAppointment);
    }

    @PostMapping("/{appointmentId}/feedback")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> submitFeedback(
            @PathVariable Integer appointmentId,
            @RequestParam float rating,
            Authentication authentication) {

        appointmentService.submitFeedback(
                appointmentId,
                rating,
                authentication.getName()
        );

        return ResponseEntity.ok("Feedback submitted successfully.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Void> deleteAppointment(
            @PathVariable Integer id,
            Authentication authentication) {

        appointmentService.deleteAppointment(id, authentication);

        return ResponseEntity.noContent().build();
    }
}