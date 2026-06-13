package com.unibuc.management.controllers;

import com.unibuc.management.dto.validation.AppointmentRequestDTO;
import com.unibuc.management.entities.*;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.services.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;
    private final MedicalServiceService medicalServiceService;
    private final DoctorService doctorService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService,
                                 PatientService patientService,
                                 MedicalServiceService medicalServiceService,
                                 DoctorService doctorService
                                 ) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
        this.medicalServiceService = medicalServiceService;
        this.doctorService = doctorService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Appointment> create(@Valid @RequestBody AppointmentRequestDTO dto, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(authentication, dto));
    }
    @GetMapping("/available-times")
    public ResponseEntity<List<OffsetDateTime>> getAvailableTimeSlots(@RequestParam Integer medicalServiceId,
                                                                      @RequestParam String date) {
        
        MedicalService medicalService = medicalServiceService.getMedicalServiceById(medicalServiceId);

        List<OffsetDateTime> availableTimeSlots = appointmentService.getAvailableTimeSlots(medicalService, date);
        return ResponseEntity.ok(availableTimeSlots);
    }
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Page<Appointment>> getAppointmentsByPatientId(
            @PathVariable Integer patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentFrom,asc") String[] sort) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        if (hasRole("ROLE_PATIENT")) {
            Patient currentPatient = patientService.getPatientByUsername(currentUsername);
            if (!currentPatient.getId().equals(patientId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        String sortBy = sort[0];
        Sort.Direction direction = (sort.length > 1 && sort[1].equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Appointment> appointmentsPage = appointmentService.getAppointmentsByPatientIdPaged(patientId, pageable);

        if (hasRole("ROLE_DOCTOR")) {
            Doctor currentDoctor = doctorService.getDoctorByUsername(currentUsername);
            Integer doctorId = currentDoctor.getId();

            List<Appointment> filtered = appointmentsPage.getContent().stream()
                    .filter(a -> a.getMedicalService().getMedicalServiceDoctors().stream()
                            .anyMatch(d -> d.getId().equals(doctorId)))
                    .toList();

            return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(
                    filtered, pageable, appointmentsPage.getTotalElements()));
        }
        return ResponseEntity.ok(appointmentsPage);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Page<Appointment>> getAppointmentsByDoctorId(
            @PathVariable Integer doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentFrom,asc") String[] sort) {

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        Doctor currentDoctor = doctorService.getDoctorByUsername(currentUsername);

        if (!currentDoctor.getId().equals(doctorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String sortBy = sort[0];
        Sort.Direction direction = (sort.length > 1 && sort[1].equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Appointment> pagedAppointments = appointmentService.getAppointmentsByDoctorIdPaged(doctorId, pageable);
        return ResponseEntity.ok(pagedAppointments);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Integer id,
                                                         @RequestParam @NotNull(message = "Noua dată este obligatorie.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime appointmentFrom) {

        if (appointmentFrom.isBefore(OffsetDateTime.now())) {
            throw new InvalidActionException("Nu puteți muta o programare în trecut.");
        }

        Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programarea cu ID-ul " + id + " nu a fost găsită."));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isOwner = false;

        if (hasRole("ROLE_PATIENT")) {
            Patient currentPatient = patientService.getPatientByUsername(currentUsername);
            isOwner = currentPatient.getId().equals(appointment.getPatient().getId());
        }
        else if (hasRole("ROLE_DOCTOR")) {
            Doctor currentDoctor = doctorService.getDoctorByUsername(currentUsername);
            Integer doctorId = currentDoctor.getId();

            isOwner = appointment.getMedicalService().getMedicalServiceDoctors().stream()
                    .anyMatch(d -> d.getId().equals(doctorId));
        }

        if (!isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MedicalService medicalService = appointment.getMedicalService();
        List<OffsetDateTime> availableSlots = appointmentService.getAvailableTimeSlots(
                medicalService,
                appointmentFrom.toLocalDate().toString()
        );

        boolean slotAvailable = availableSlots.stream().anyMatch(slot ->
                slot.truncatedTo(ChronoUnit.MINUTES)
                        .equals(appointmentFrom.truncatedTo(ChronoUnit.MINUTES))
        );

        if (!slotAvailable) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        appointment.setAppointmentFrom(appointmentFrom);
        Appointment updatedAppointment = appointmentService.save(appointment);

        return ResponseEntity.ok(updatedAppointment);
    }
    @PostMapping("/{appointmentId}/feedback")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> submitFeedback(@PathVariable Integer appointmentId,
                                                 @RequestParam float rating) {
        Optional<Appointment> appointmentOpt = appointmentService.findById(appointmentId);

        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Appointment not found or already completed.");
        }

        Appointment appointment = appointmentOpt.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Patient currentPatient = patientService.getPatientByUsername(currentUsername);

        if (!currentPatient.getId().equals(appointment.getPatient().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You can only submit feedback for your own appointments.");
        }

        OffsetDateTime currentTime = OffsetDateTime.now();
        if (currentTime.isBefore(appointment.getAppointmentFrom().plusMinutes(30))) {
            return ResponseEntity.badRequest().body("Feedback can only be submitted after 30 minutes from the appointment start time.");
        }

        MedicalService service = appointment.getMedicalService();
        double newRating = (service.getRating() * service.getNrOfRatings() + rating) / (service.getNrOfRatings() + 1);
        service.setRating(newRating);
        service.setNrOfRatings(service.getNrOfRatings() + 1);
        medicalServiceService.save(service);

        appointment.setStatus("Completed");
        appointmentService.save(appointment);

        return ResponseEntity.ok("Feedback submitted successfully.");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Integer id) {
        Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programarea cu ID-ul " + id + " nu a fost găsită."));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isOwner = false;

        if (hasRole("ROLE_PATIENT")) {
            Patient currentPatient = patientService.getPatientByUsername(currentUsername);
            if (currentPatient.getId().equals(appointment.getPatient().getId())) {
                isOwner = true;
            }
        } else if (hasRole("ROLE_DOCTOR")) {
            Doctor currentDoctor = doctorService.getDoctorByUsername(currentUsername);
            Integer doctorId = currentDoctor.getId();

            isOwner = appointment.getMedicalService().getMedicalServiceDoctors().stream()
                    .anyMatch(d -> d.getId().equals(doctorId));
        }

        if (!isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    private Patient getAuthenticatedPatient() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return patientService.getPatientByUsername(username);
    }

    private boolean hasRole(String roleName) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(roleName));
    }
}