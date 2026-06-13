package com.unibuc.management.services;

import com.unibuc.management.dto.validation.PatientRequestDTO;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.PatientRepository;
import com.unibuc.management.repositories.UserRepository;
import com.unibuc.management.repositories.InsuranceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InsuranceProviderRepository insuranceProviderRepository;

    private PatientService patientService;

    private Patient patient;
    private PatientRequestDTO patientDTO;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository, userRepository, insuranceProviderRepository);

        LocalDate validBirthDate = LocalDate.now().minusYears(25);

        patient = new Patient();
        patient.setId(1);
        patient.setName("John Doe");
        patient.setAge(validBirthDate);
        patient.setSex(true);
        patient.setSubscription(false);

        patientDTO = new PatientRequestDTO();
        patientDTO.setName("John Doe");
        patientDTO.setBirthDate(validBirthDate);
        patientDTO.setSex(true);
        patientDTO.setSubscription(false);
        patientDTO.setInsuranceProviderId(null);
    }

    @Test
    void testGetAllPatients() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        List<Patient> patients = patientService.getAllPatients();

        assertNotNull(patients);
        assertFalse(patients.isEmpty());
        assertEquals(1, patients.size());
        assertEquals(patient.getId(), patients.get(0).getId());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void testGetPatientById_Found() {
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));

        Patient foundPatient = patientService.getPatientById(1);

        assertNotNull(foundPatient);
        assertEquals(patient.getId(), foundPatient.getId());
        verify(patientRepository, times(1)).findById(1);
    }

    @Test
    void testGetPatientById_NotFound() {
        when(patientRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> patientService.getPatientById(1));
        verify(patientRepository, times(1)).findById(1);
    }

    @Test
    void testCreatePatient_Success() {
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        Patient createdPatient = patientService.createPatient(patientDTO);

        assertNotNull(createdPatient);
        assertEquals(patient.getId(), createdPatient.getId());
        assertEquals(patient.getName(), createdPatient.getName());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void testCreatePatient_UnderageThrowsException() {
        patientDTO.setBirthDate(LocalDate.now().minusYears(16));

        assertThrows(InvalidActionException.class, () -> {
            patientService.createPatient(patientDTO);
        });

        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testUpdatePatient_Found() {
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        Patient updatedPatient = patientService.updatePatient(1, patientDTO);

        assertNotNull(updatedPatient);
        verify(patientRepository, times(1)).findById(1);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }
    @Test
    void testUpdatePatient_NotFound() {
        when(patientRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            patientService.updatePatient(1, patientDTO);
        });

        verify(patientRepository, times(1)).findById(1);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void testDeletePatient_Success() {
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        doNothing().when(patientRepository).delete(patient);

        assertDoesNotThrow(() -> patientService.deletePatient(1));

        verify(patientRepository, times(1)).findById(1);
        verify(patientRepository, times(1)).delete(patient);
    }

    @Test
    void testDeletePatient_NotFound() {
        when(patientRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> patientService.deletePatient(1));

        verify(patientRepository, times(1)).findById(1);
        verify(patientRepository, never()).delete(any(Patient.class));
    }
}