package com.unibuc.management.services;

import com.unibuc.management.clients.MedicalClient;
import com.unibuc.management.domain.User;
import com.unibuc.management.dto.LoginRequest;
import com.unibuc.management.dto.RegisterRequest;
import com.unibuc.management.dto.internal.InternalPatientRequest;
import com.unibuc.management.dto.internal.ProfileIdDTO;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

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
    private com.unibuc.management.repositories.UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MedicalClient medicalClient;
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
    }

    @Test
    void registerUser_shouldRegisterPatientSuccessfully() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.registerUser(registerRequest);

        verify(userRepository).save(any(User.class));
        verify(medicalClient).createPatient(any(InternalPatientRequest.class));
    }

    @Test
    void registerUser_shouldThrow_whenUsernameExists() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        assertThrows(InvalidActionException.class,
                () -> authService.registerUser(registerRequest));
    }

    @Test
    void registerUser_shouldThrow_whenInvalidRole() {
        registerRequest.setRole("ADMIN123");
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(InvalidActionException.class,
                () -> authService.registerUser(registerRequest));
    }

    @Test
    void registerUser_shouldThrow_whenPatientUnder18() {
        registerRequest.setAge(LocalDate.now().minusYears(10));
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(InvalidActionException.class,
                () -> authService.registerUser(registerRequest));
    }

    @Test
    void login_shouldReturnPatientProfile() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        doReturn(Collections.singleton(Role.PATIENT)).when(authentication).getAuthorities();
        when(medicalClient.getPatientByUsername("john"))
                .thenReturn(ProfileIdDTO.builder().id(1).build());

        Map<String, Object> result = authService.login(loginRequest, httpRequest, httpResponse);

        assertEquals("john", result.get("username"));
        assertEquals(1L, result.get("profileId"));
        verify(securityContextRepository).saveContext(any(), eq(httpRequest), eq(httpResponse));
    }

    @Test
    void login_shouldReturnDoctorProfile() {
        loginRequest.setUsername("doctor");
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        doReturn(Collections.singleton(Role.DOCTOR)).when(authentication).getAuthorities();
        when(medicalClient.getDoctorByUsername("doctor"))
                .thenReturn(ProfileIdDTO.builder().id(2).build());

        Map<String, Object> result = authService.login(loginRequest, httpRequest, httpResponse);

        assertEquals(2L, result.get("profileId"));
    }

    @Test
    void getCurrentUser_shouldReturnPatient() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(medicalClient.getPatientByUsername("john"))
                .thenReturn(ProfileIdDTO.builder().id(1).build());

        Map<String, Object> result = authService.getCurrentUser("john");

        assertEquals("john", result.get("username"));
        assertEquals(1, result.get("patientId"));
    }

    @Test
    void getCurrentUser_shouldThrow_whenUserNotFound() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.getCurrentUser("john"));
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        authService.deleteUser("john");

        verify(medicalClient).deletePatientByUser(1L);
        verify(userRepository).delete(user);
    }
}
