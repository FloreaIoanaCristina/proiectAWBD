package com.unibuc.management.controllers;

import com.unibuc.management.dto.ScheduleEntry;
import com.unibuc.management.dto.request.PtoRequestDTO;
import com.unibuc.management.services.DoctorScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/doctor-schedule")
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    @GetMapping("/day")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<List<ScheduleEntry>> getDoctorScheduleForDay(
            @RequestParam Integer doctorId,
            @RequestParam String date) {

        return ResponseEntity.ok(
                doctorScheduleService.getDoctorScheduleForDay(doctorId, date)
        );
    }

    @GetMapping("/pto/{doctorId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<List<ScheduleEntry>> getDoctorLeaves(@PathVariable Integer doctorId) {
        List<ScheduleEntry> leaves = doctorScheduleService.getDoctorLeaves(doctorId);
        return ResponseEntity.ok(leaves);
    }

    @PostMapping("/schedulePTO")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> schedulePTO(
            @Valid @RequestBody PtoRequestDTO dto) {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        doctorScheduleService.schedulePTO(dto, username);

        return ResponseEntity.ok("Concediu programat cu succes!");
    }

    @PutMapping("/{ptoId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> updatePTO(@PathVariable Integer ptoId,
                                            @Valid @RequestBody PtoRequestDTO dto) {

        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        doctorScheduleService.updatePTO(ptoId, dto, currentUsername);

        return ResponseEntity.ok("Concediu actualizat cu succes!");
    }

    @DeleteMapping("/{ptoId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deletePTO(@PathVariable Integer ptoId) {
        String currentUsername = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        doctorScheduleService.deletePTO(ptoId, currentUsername);
        return ResponseEntity.noContent().build();
    }
}