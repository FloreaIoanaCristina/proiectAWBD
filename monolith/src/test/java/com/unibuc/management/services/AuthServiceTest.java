package com.unibuc.management.services;

import com.unibuc.management.domain.Doctor;
import com.unibuc.management.domain.MedicalService;
import com.unibuc.management.domain.Patient;
import com.unibuc.management.domain.User;
import com.unibuc.management.dto.LoginRequest;
import com.unibuc.management.dto.RegisterRequest;
import com.unibuc.management.dto.request.PatientRequestDTO;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.UserRepository;
import com.unibuc.management.security.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import com.unibuc.management.exceptions.ResourceNotFoundException;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PatientService patientService;
    @Mock
    private DoctorService doctorService;
    @Mock
    private MedicalServiceRepository medicalServiceRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private HttpServletRequest httpRequest;
    @Mock
    private HttpServletResponse httpResponse;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private Patient patient;
    private Doctor doctor;
    private MedicalService medicalService;

    @BeforeEach
    void setUp() {

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("john");
        registerRequest.setPassword("pass");
        registerRequest.setRole("PATIENT");
        registerRequest.setFullName("John Doe");
        registerRequest.setAge(LocalDate.of(1990, 1, 1));
        registerRequest.setSex(true);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("john");
        loginRequest.setPassword("pass");

        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setPassword("encoded");
        user.setRole(Role.PATIENT);

        patient = new Patient();
        patient.setId(1);

        doctor = new Doctor();
        doctor.setId(2);

        medicalService = new MedicalService();
        medicalService.setId(1);
        medicalService.setName("Cardiology");
    }
    @Test
    void registerUser_shouldRegisterPatientSuccessfully() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("pass"))
                .thenReturn("encoded");

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        authService.registerUser(registerRequest);

        verify(userRepository).save(any(User.class));
        verify(patientService).createPatient(any(PatientRequestDTO.class));
    }
    @Test
    void registerUser_shouldThrow_whenUsernameExists() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThrows(InvalidActionException.class,
                () -> authService.registerUser(registerRequest));
    }

    @Test
    void registerUser_shouldThrow_whenInvalidRole() {

        registerRequest.setRole("ADMIN123");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidActionException.class,
                () -> authService.registerUser(registerRequest));
    }

    @Test
    void registerUser_shouldThrow_whenPatientUnder18() {

        registerRequest.setAge(LocalDate.now().minusYears(10));

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidActionException.class,
                () -> authService.registerUser(registerRequest));
    }
    @Test
    void login_shouldReturnPatientProfile() {

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        doReturn(Collections.singleton(Role.PATIENT))
                .when(authentication)
                .getAuthorities();

        when(patientService.getPatientByUsername("john"))
                .thenReturn(patient);

        Map<String,Object> result =
                authService.login(loginRequest, httpRequest, httpResponse);

        assertEquals("john", result.get("username"));
        assertEquals(1L, result.get("profileId"));

        verify(securityContextRepository)
                .saveContext(any(), eq(httpRequest), eq(httpResponse));
    }
    @Test
    void login_shouldReturnDoctorProfile() {

        loginRequest.setUsername("doctor");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        doReturn(Collections.singleton(Role.DOCTOR))
                .when(authentication)
                .getAuthorities();

        when(doctorService.getDoctorByUsername("doctor"))
                .thenReturn(doctor);

        Map<String,Object> result =
                authService.login(loginRequest, httpRequest, httpResponse);

        assertEquals(2L, result.get("profileId"));
    }
    @Test
    void getCurrentUser_shouldReturnPatient() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(patientService.getPatientByUsername("john"))
                .thenReturn(patient);

        Map<String,Object> result =
                authService.getCurrentUser("john");

        assertEquals("john", result.get("username"));
        assertEquals(1, result.get("patientId"));
    }
    @Test
    void getCurrentUser_shouldThrow_whenUserNotFound() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.getCurrentUser("john"));
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() {

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        authService.deleteUser("john");

        verify(userRepository).delete(user);
    }
}