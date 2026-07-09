package com.unibuc.management.controllers;

import com.unibuc.management.dto.internal.InternalDoctorRequest;
import com.unibuc.management.dto.internal.InternalPatientRequest;
import com.unibuc.management.dto.internal.PricingInfoDTO;
import com.unibuc.management.dto.internal.ProfileRef;
import com.unibuc.management.dto.request.DoctorRequestDTO;
import com.unibuc.management.dto.request.PatientRequestDTO;
import com.unibuc.management.dto.response.DoctorResponseDTO;
import com.unibuc.management.dto.response.PatientResponseDTO;
import com.unibuc.management.dto.summary.AppointmentSummaryDTO;
import com.unibuc.management.services.AppointmentService;
import com.unibuc.management.services.DoctorService;
import com.unibuc.management.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @PostMapping("/patients")
    public ResponseEntity<PatientResponseDTO> createPatient(@RequestBody InternalPatientRequest req) {
        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setName(req.getName());
        dto.setBirthDate(req.getBirthDate());
        dto.setMedicalRecord(req.getMedicalRecord());
        dto.setSex(req.getSex());
        dto.setSubscription(req.getSubscription());
        dto.setInsuranceProviderId(req.getInsuranceProviderId());
        dto.setUserId(req.getUserId());
        dto.setUsername(req.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.createPatient(dto));
    }

    @PostMapping("/doctors")
    public ResponseEntity<DoctorResponseDTO> createDoctor(@RequestBody InternalDoctorRequest req) {
        DoctorRequestDTO dto = new DoctorRequestDTO();
        dto.setName(req.getName());
        dto.setOffice(req.getOffice());
        dto.setNumberOfPtodays(req.getNumberOfPtodays());
        dto.setMedicalServiceId(req.getMedicalServiceId());
        dto.setUserId(req.getUserId());
        dto.setUsername(req.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createDoctor(dto));
    }

    @GetMapping("/patients/by-username/{username}")
    public ResponseEntity<ProfileRef> getPatientByUsername(@PathVariable String username) {
        return ResponseEntity.ok(ProfileRef.builder()
                .id(patientService.getPatientByUsername(username).getId())
                .build());
    }

    @GetMapping("/doctors/by-username/{username}")
    public ResponseEntity<ProfileRef> getDoctorByUsername(@PathVariable String username) {
        return ResponseEntity.ok(ProfileRef.builder()
                .id(doctorService.getDoctorByUsername(username).getId())
                .build());
    }

    @DeleteMapping("/patients/by-user/{userId}")
    public ResponseEntity<Void> deletePatientByUser(@PathVariable Long userId) {
        patientService.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/doctors/by-user/{userId}")
    public ResponseEntity<Void> deleteDoctorByUser(@PathVariable Long userId) {
        doctorService.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/appointments/{id}/pricing-info")
    public ResponseEntity<PricingInfoDTO> getPricingInfo(@PathVariable Integer id) {
        return ResponseEntity.ok(appointmentService.getPricingInfo(id));
    }

    @GetMapping("/appointments/{id}/summary")
    public ResponseEntity<AppointmentSummaryDTO> getAppointmentSummary(@PathVariable Integer id) {
        return ResponseEntity.ok(appointmentService.getAppointmentSummary(id));
    }
}
