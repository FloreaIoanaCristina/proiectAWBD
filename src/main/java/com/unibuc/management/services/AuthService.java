package com.unibuc.management.services;

import com.unibuc.management.dto.RegisterRequest;
import com.unibuc.management.dto.validation.DoctorRequestDTO;
import com.unibuc.management.dto.validation.PatientRequestDTO;
import com.unibuc.management.entities.*;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.UserRepository;
import com.unibuc.management.security.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final MedicalServiceRepository medicalServiceRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       PatientService patientService,
                       DoctorService doctorService,
                       MedicalServiceRepository medicalServiceRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.medicalServiceRepository = medicalServiceRepository;
    }

    @Transactional
    public void registerUser(RegisterRequest request) {
        log.debug("Se încearcă înregistrarea unui utilizator nou cu username-ul: {} și rolul: {}",
                request.getUsername(), request.getRole());
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.error("Înregistrare eșuată: Username-ul '{}' este deja utilizat în sistem.", request.getUsername());
            throw new InvalidActionException("Utilizatorul deja există.");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Înregistrare eșuată: Rolul specificat '{}' nu este valid.", request.getRole());
            throw new InvalidActionException("Rolul specificat nu este valid.");
        }

        if (role == Role.PATIENT && request.getAge() != null) {
            if (Period.between(request.getAge(), LocalDate.now()).getYears() < 18) {
                log.error("Înregistrare eșuată: Pacientul nu are cel putin 18 ani (născut la {}).",
                    request.getAge());
                throw new InvalidActionException("Pacienții trebuie să aibă minim 18 ani.");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        User savedUser = userRepository.save(user);

        log.debug("Entitatea User pentru '{}' a fost salvată în baza de date cu ID-ul: {}",
                savedUser.getUsername(), savedUser.getId());

        if (role == Role.PATIENT) {
            createPatientProfile(request, savedUser);
        } else if (role == Role.DOCTOR) {
            createDoctorProfile(request, savedUser);
        }

        log.info("Înregistrare reușită: Utilizatorul '{}' cu rolul [{}] a fost creat cu succes.",
                savedUser.getUsername(), savedUser.getRole());
    }

    private void createPatientProfile(RegisterRequest request, User user) {
        log.debug("Se generează profilul de Pacient pentru user-ul: {}", user.getUsername());
        PatientRequestDTO patientDTO = new PatientRequestDTO();
        patientDTO.setName(request.getFullName() != null ? request.getFullName() : user.getUsername());
        patientDTO.setBirthDate(request.getAge() != null ? request.getAge() : LocalDate.now().minusYears(20));
        patientDTO.setSex(request.getSex() != null ? request.getSex() : true);
        patientDTO.setSubscription(false);
        patientDTO.setInsuranceProviderId(null);

        patientDTO.setUserId(user.getId());

        patientService.createPatient(patientDTO);
        log.debug("Profilul de Pacient salvat cu succes pentru user-ul: {}", user.getUsername());
    }

    private void createDoctorProfile(RegisterRequest request, User user) {
        log.debug("Se generează profilul de Medic pentru user-ul: {}", user.getUsername());

        MedicalService service = medicalServiceRepository.findById(request.getMedicalServiceId() != null ? request.getMedicalServiceId() : 1)
                .orElseGet(() -> {
                    log.warn("Serviciul medical solicitat (ID: {}) nu a fost găsit. Se încearcă alocarea primului serviciu disponibil.",
                            request.getMedicalServiceId());
                    return medicalServiceRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> {
                                log.error("Înregistrare Medic eșuată critic: Nu există niciun serviciu medical configurat în baza de date!");
                                return new InvalidActionException("Nu există servicii medicale configurate.");
                            });
                });
        DoctorRequestDTO doctorDTO = new DoctorRequestDTO();
        doctorDTO.setName(request.getFullName() != null ? request.getFullName() : "Dr. " + user.getUsername());
        doctorDTO.setOffice(request.getOffice() != null ? request.getOffice() : "Cabinet 101");
        doctorDTO.setNumberOfPtodays(request.getNumberOfPTOdays() != null ? request.getNumberOfPTOdays() : 21);
        doctorDTO.setMedicalServiceId(service.getId());
        doctorDTO.setUserId(user.getId());

        doctorService.saveDoctor(doctorDTO);

        log.debug("Profilul de Medic asociat serviciului '{}' a fost salvat pentru: {}",
                service.getName(), user.getUsername());
    }
}