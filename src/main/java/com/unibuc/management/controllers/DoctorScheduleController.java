package com.unibuc.management.controllers;

import com.unibuc.management.dto.ScheduleEntry;
import com.unibuc.management.dto.validation.PtoRequestDTO;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.services.DoctorScheduleService;
import com.unibuc.management.services.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;


@RestController
@RequestMapping("/api/doctor-schedule")
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;
    private final DoctorService doctorService;

    public DoctorScheduleController(DoctorScheduleService doctorScheduleService, DoctorService doctorService) {
        this.doctorScheduleService = doctorScheduleService;
        this.doctorService = doctorService;
    }

    @GetMapping("/day")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<List<ScheduleEntry>> getDoctorScheduleForDay(
            @RequestParam Integer doctorId,
            @RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<ScheduleEntry> schedule = doctorScheduleService.getDoctorScheduleForDay(doctorId, localDate);
            return ResponseEntity.ok(schedule);
        } catch (DateTimeParseException e) {
            throw new InvalidActionException("Formatul datei este invalid. Folosiți YYYY-MM-DD.");
        }
    }

    @PostMapping("/schedulePTO")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> schedulePTO(@Valid @RequestBody PtoRequestDTO pto) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Doctor currentDoctor = doctorService.getDoctorByUsername(currentUsername);

        if (!currentDoctor.getId().equals(pto.getDoctorId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Puteți programa concediu doar pentru propria persoană.");
        }

        if (pto.getEndDate().isBefore(pto.getStartDate())) {
            throw new InvalidActionException("Data de sfârșit nu poate fi înainte de data de început.");
        }

        doctorScheduleService.schedulePTO(pto.getDoctorId(), pto.getStartDate(), pto.getEndDate());

        return ResponseEntity.ok("Concediu programat cu succes!");
    }
    @PutMapping("/{ptoId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updatePTO(@PathVariable Integer ptoId,
                                       @Valid @RequestBody PtoRequestDTO dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new InvalidActionException("Data de sfârșit nu poate fi înainte de data de început.");
        }

        doctorScheduleService.updatePTO(ptoId, dto.getStartDate(), dto.getEndDate());
        return ResponseEntity.ok("Concediu actualizat cu succes!");
    }

    @DeleteMapping("/{ptoId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deletePTO(@PathVariable Integer ptoId) {
        doctorScheduleService.deletePTO(ptoId);
        return ResponseEntity.noContent().build();
    }
}