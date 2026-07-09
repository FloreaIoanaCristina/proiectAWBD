package com.unibuc.management.clients;

import com.unibuc.management.dto.internal.InternalDoctorRequest;
import com.unibuc.management.dto.internal.InternalPatientRequest;
import com.unibuc.management.dto.internal.ProfileIdDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "medical-service", path = "/api/internal", fallback = MedicalClientFallback.class)
public interface MedicalClient {

    @PostMapping("/patients")
    void createPatient(@RequestBody InternalPatientRequest req);

    @PostMapping("/doctors")
    void createDoctor(@RequestBody InternalDoctorRequest req);

    @GetMapping("/patients/by-username/{username}")
    ProfileIdDTO getPatientByUsername(@PathVariable("username") String username);

    @GetMapping("/doctors/by-username/{username}")
    ProfileIdDTO getDoctorByUsername(@PathVariable("username") String username);

    @DeleteMapping("/patients/by-user/{userId}")
    void deletePatientByUser(@PathVariable("userId") Long userId);

    @DeleteMapping("/doctors/by-user/{userId}")
    void deleteDoctorByUser(@PathVariable("userId") Long userId);
}
