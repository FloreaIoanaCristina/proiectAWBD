package com.unibuc.management.services;

import com.unibuc.management.domain.InsuranceProvider;
import com.unibuc.management.domain.User;
import com.unibuc.management.dto.request.PatientRequestDTO;
import com.unibuc.management.domain.Patient;
import com.unibuc.management.dto.response.PatientResponseDTO;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.PatientRepository;
import com.unibuc.management.repositories.UserRepository;
import com.unibuc.management.repositories.InsuranceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @InjectMocks
    private PatientService patientService;

    @Mock private PatientRepository patientRepository;
    @Mock private UserRepository userRepository;
    @Mock private InsuranceProviderRepository insuranceProviderRepository;
    @Mock private AppointmentRepository appointmentRepository;

    private Patient patient;
    private PatientRequestDTO dto;
    private InsuranceProvider provider;
    private User user;

    @BeforeEach
    void setUp() {

        patient = new Patient();
        patient.setId(1);

        provider = new InsuranceProvider();
        user = new User();
        user.setId(1L);
        user.setUsername("user");

        dto = new PatientRequestDTO();
        dto.setName("John");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        dto.setSex(true);
        dto.setSubscription(true);
    }

    private void mockAuth(String username, String role) {
        var auth = mock(org.springframework.security.core.Authentication.class);

        when(auth.getName()).thenReturn(username);
        when(auth.getAuthorities()).thenAnswer(inv -> List.of(
                (org.springframework.security.core.GrantedAuthority)
                        () -> role
        ));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getPatientById_shouldReturnPatient() {

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        Patient result = patientService.getPatientById(1);

        assertEquals(1, result.getId());
    }

    @Test
    void getPatientById_shouldThrow_whenNotFound() {

        when(patientRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientService.getPatientById(1));
    }

    @Test
    void getPatientByUsername_shouldReturnPatient() {

        when(patientRepository.findByUserUsername("user"))
                .thenReturn(Optional.of(patient));

        Patient result = patientService.getPatientByUsername("user");

        assertEquals(1, result.getId());
    }

    @Test
    void createPatient_shouldCreateSuccessfully() {

        dto.setInsuranceProviderId(1);
        dto.setUserId(1L);

        when(insuranceProviderRepository.findById(1))
                .thenReturn(Optional.of(provider));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(patientRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        PatientResponseDTO result = patientService.createPatient(dto);

        assertEquals(provider.getId(), result.getInsuranceProvider().getId());
        assertEquals(user.getId(), result.getUserId());
    }

    @Test
    void createPatient_shouldThrow_whenUnder18() {

        dto.setBirthDate(LocalDate.now().minusYears(10));

        assertThrows(InvalidActionException.class,
                () -> patientService.createPatient(dto));
    }

    @Test
    void createPatient_shouldThrow_whenInsuranceNotFound() {

        dto.setInsuranceProviderId(1);

        when(insuranceProviderRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientService.createPatient(dto));
    }


    @Test
    void updatePatient_shouldThrow_whenNotFound() {

        when(patientRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientService.updatePatient(1, dto));
    }

    @Test
    void updatePatient_shouldUpdateSuccessfully() {

        Patient existing = new Patient();
        existing.setId(1);

        dto.setName("Updated");

        when(patientRepository.findById(1)).thenReturn(Optional.of(existing));
        when(patientRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        PatientResponseDTO result = patientService.updatePatient(1, dto);

        assertEquals("Updated", result.getName());
    }

    @Test
    void updatePatient_shouldClearInsurance_whenNull() {

        Patient existing = new Patient();
        existing.setId(1);
        existing.setInsuranceProvider(provider);

        dto.setInsuranceProviderId(null);

        when(patientRepository.findById(1)).thenReturn(Optional.of(existing));
        when(patientRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        PatientResponseDTO result = patientService.updatePatient(1, dto);

        assertNull(result.getInsuranceProvider());
    }

    @Test
    void deletePatient_shouldThrow_whenHasAppointments() {

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(appointmentRepository.countByPatientId(1)).thenReturn(2L);

        assertThrows(InvalidActionException.class,
                () -> patientService.deletePatient(1));
    }

    @Test
    void deletePatient_shouldDeleteSuccessfully_asPatientOwner() {

        patient.setUser(user);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(appointmentRepository.countByPatientId(1)).thenReturn(0L);

        mockAuth("user", "ROLE_PATIENT");

        patientService.deletePatient(1);

        verify(patientRepository).delete(patient);
        verify(userRepository).delete(user);
    }
}