package com.unibuc.management.clients;

import com.unibuc.management.dto.internal.InternalDoctorRequest;
import com.unibuc.management.dto.internal.InternalPatientRequest;
import com.unibuc.management.dto.internal.ProfileIdDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilience4j fallback for medical-service. Profile creation cannot be silently
 * skipped (it would orphan the user account), so create calls surface an error;
 * lookups degrade to null.
 */
@Slf4j
@Component
public class MedicalClientFallback implements MedicalClient {

    @Override
    public void createPatient(InternalPatientRequest req) {
        log.error("medical-service indisponibil. Nu s-a putut crea profilul de pacient pentru {}.",
                req != null ? req.getUsername() : null);
        throw new IllegalStateException("Serviciul medical este momentan indisponibil. Reîncercați înregistrarea mai târziu.");
    }

    @Override
    public void createDoctor(InternalDoctorRequest req) {
        log.error("medical-service indisponibil. Nu s-a putut crea profilul de doctor pentru {}.",
                req != null ? req.getUsername() : null);
        throw new IllegalStateException("Serviciul medical este momentan indisponibil. Reîncercați înregistrarea mai târziu.");
    }

    @Override
    public ProfileIdDTO getPatientByUsername(String username) {
        log.warn("medical-service indisponibil. Nu se poate obține profilul de pacient pentru {}.", username);
        return null;
    }

    @Override
    public ProfileIdDTO getDoctorByUsername(String username) {
        log.warn("medical-service indisponibil. Nu se poate obține profilul de doctor pentru {}.", username);
        return null;
    }

    @Override
    public void deletePatientByUser(Long userId) {
        log.warn("medical-service indisponibil. Profilul de pacient pentru User ID {} nu a putut fi șters acum.", userId);
    }

    @Override
    public void deleteDoctorByUser(Long userId) {
        log.warn("medical-service indisponibil. Profilul de doctor pentru User ID {} nu a putut fi șters acum.", userId);
    }
}
