package com.unibuc.management.controllers;

import com.unibuc.management.dto.LoginRequest;
import com.unibuc.management.dto.RegisterRequest;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.entities.User;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.UserRepository;
import com.unibuc.management.security.Role;
import com.unibuc.management.services.AuthService;
import com.unibuc.management.services.DoctorService;
import com.unibuc.management.services.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.GrantedAuthority;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final MedicalServiceRepository medicalServiceRepository;
    private final SecurityContextRepository securityContextRepository;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthenticationManager authenticationManager,
                          AuthService authService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          PatientService patientService,
                          DoctorService doctorService,
                          MedicalServiceRepository medicalServiceRepository,
                          HttpSessionSecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.medicalServiceRepository = medicalServiceRepository;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok(Map.of("message", "Cont creat cu succes"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Userul nu a fost găsit"));

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user.getUsername());
        userData.put("role", user.getRole());

        if (user.getRole() == Role.PATIENT) {
            Patient p = patientService.getPatientByUsername(username);
            userData.put("patientId", p.getId());
        } else if (user.getRole() == Role.DOCTOR) {
            Doctor d = doctorService.getDoctorByUsername(username);
            userData.put("doctorId", d.getId());
        }

        return ResponseEntity.ok(userData);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String role = authentication.getAuthorities().iterator().next().getAuthority();
        Long profileId = null;

        if (role.contains("PATIENT")) {
            profileId = patientService.getPatientByUsername(request.getUsername()).getId().longValue();
        } else if (role.contains("DOCTOR")) {
            profileId = doctorService.getDoctorByUsername(request.getUsername()).getId().longValue();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "username", request.getUsername(),
                "roles", roles,
                "profileId", profileId
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<?> delete(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok("User deleted");
                })
                .orElse(ResponseEntity.notFound().build());
    }


}