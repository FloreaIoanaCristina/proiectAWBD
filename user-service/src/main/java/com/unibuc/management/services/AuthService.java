package com.unibuc.management.services;

import com.unibuc.management.clients.MedicalClient;
import com.unibuc.management.dto.LoginRequest;
import com.unibuc.management.dto.RegisterRequest;
import com.unibuc.management.dto.internal.InternalDoctorRequest;
import com.unibuc.management.dto.internal.InternalPatientRequest;
import com.unibuc.management.dto.internal.ProfileIdDTO;
import com.unibuc.management.domain.User;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.UserRepository;
import com.unibuc.management.security.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MedicalClient medicalClient;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

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
        log.debug("Se solicită medical-service crearea profilului de Pacient pentru user-ul: {}", user.getUsername());
        InternalPatientRequest patientRequest = InternalPatientRequest.builder()
                .name(request.getFullName() != null ? request.getFullName() : user.getUsername())
                .birthDate(request.getAge() != null ? request.getAge() : LocalDate.now().minusYears(20))
                .sex(request.getSex() != null ? request.getSex() : true)
                .subscription(false)
                .insuranceProviderId(null)
                .userId(user.getId())
                .username(user.getUsername())
                .build();

        medicalClient.createPatient(patientRequest);
        log.debug("Profilul de Pacient a fost creat cu succes în medical-service pentru user-ul: {}", user.getUsername());
    }

    private void createDoctorProfile(RegisterRequest request, User user) {
        log.debug("Se solicită medical-service crearea profilului de Medic pentru user-ul: {}", user.getUsername());

        InternalDoctorRequest doctorRequest = InternalDoctorRequest.builder()
                .name(request.getFullName() != null ? request.getFullName() : "Dr. " + user.getUsername())
                .office(request.getOffice() != null ? request.getOffice() : "Cabinet 101")
                .numberOfPtodays(request.getNumberOfPTOdays() != null ? request.getNumberOfPTOdays() : 21)
                .medicalServiceId(request.getMedicalServiceId() != null ? request.getMedicalServiceId() : 1)
                .userId(user.getId())
                .username(user.getUsername())
                .build();

        medicalClient.createDoctor(doctorRequest);

        log.debug("Profilul de Medic a fost creat cu succes în medical-service pentru: {}", user.getUsername());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> login(LoginRequest request,
                                     HttpServletRequest httpRequest,
                                     HttpServletResponse httpResponse) {

        log.debug("Autentificare pentru utilizatorul '{}'.", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        Long profileId = null;

        if (role.contains("PATIENT")) {
            ProfileIdDTO profile = medicalClient.getPatientByUsername(request.getUsername());
            profileId = profile != null && profile.getId() != null ? profile.getId().longValue() : null;
        } else if (role.contains("DOCTOR")) {
            ProfileIdDTO profile = medicalClient.getDoctorByUsername(request.getUsername());
            profileId = profile != null && profile.getId() != null ? profile.getId().longValue() : null;
        }

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .toList();

        log.info("Utilizatorul '{}' s-a autentificat cu succes.", request.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("username", request.getUsername());
        response.put("roles", roles);
        response.put("profileId", profileId);
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Utilizatorul '{}' nu a fost găsit.", username);
                    return new ResourceNotFoundException("Userul nu a fost găsit.");
                });

        Map<String, Object> userData = new HashMap<>();

        userData.put("username", user.getUsername());
        userData.put("role", user.getRole());

        if (user.getRole() == Role.PATIENT) {
            ProfileIdDTO profile = medicalClient.getPatientByUsername(username);
            userData.put("patientId", profile != null ? profile.getId() : null);
        } else if (user.getRole() == Role.DOCTOR) {
            ProfileIdDTO profile = medicalClient.getDoctorByUsername(username);
            userData.put("doctorId", profile != null ? profile.getId() : null);
        }
        return userData;
    }

    /**
     * Full account deletion initiated by the user: removes the medical profile
     * (via medical-service) and then the user account.
     */
    @Transactional
    public void deleteUser(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Ștergere eșuată. Utilizatorul '{}' nu există.", username);
                    return new ResourceNotFoundException("Userul nu a fost găsit.");
                });

        if (user.getRole() == Role.PATIENT) {
            medicalClient.deletePatientByUser(user.getId());
        } else if (user.getRole() == Role.DOCTOR) {
            medicalClient.deleteDoctorByUser(user.getId());
        }

        userRepository.delete(user);

        log.info("Utilizatorul '{}' a fost șters.", username);
    }

    /**
     * Account deletion triggered internally by medical-service (when a
     * doctor/patient profile is removed there). Deletes only the user account,
     * with no callback to medical-service, to avoid a delete loop.
     */
    @Transactional
    public void deleteUserInternal(Long userId) {
        userRepository.findById(userId).ifPresentOrElse(
                user -> {
                    userRepository.delete(user);
                    log.info("Contul de utilizator ID {} a fost șters (declanșat de medical-service).", userId);
                },
                () -> log.warn("Ștergere internă: utilizatorul cu ID {} nu există deja.", userId)
        );
    }
}
