package com.unibuc.management.services;

import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.MedicalServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalServiceServiceTest {

    @Mock
    private MedicalServiceRepository medicalServiceRepository;

    private MedicalServiceService medicalServiceService;

    private MedicalService medicalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        medicalServiceService = new MedicalServiceService(medicalServiceRepository);

        medicalService = new MedicalService();
        medicalService.setId(1);
        medicalService.setName("General Consultation");
    }

    @Test
    void testGetById_Found() {
        when(medicalServiceRepository.findById(1)).thenReturn(Optional.of(medicalService));

        MedicalService foundService = medicalServiceService.getMedicalServiceById(1);

        assertNotNull(foundService);
        assertEquals(medicalService.getId(), foundService.getId());
        assertEquals(medicalService.getName(), foundService.getName());
        verify(medicalServiceRepository, times(1)).findById(1);
    }

    @Test
    void testGetById_NotFound() {
        when(medicalServiceRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            medicalServiceService.getMedicalServiceById(1);
        });

        verify(medicalServiceRepository, times(1)).findById(1);
    }

    @Test
    void testSaveMedicalService() {
        when(medicalServiceRepository.save(medicalService)).thenReturn(medicalService);

        MedicalService savedService = medicalServiceService.save(medicalService);

        assertNotNull(savedService);
        assertEquals(medicalService.getId(), savedService.getId());
        verify(medicalServiceRepository, times(1)).save(medicalService);
    }

    @Test
    void testDelete_Found() {
        when(medicalServiceRepository.findById(1)).thenReturn(Optional.of(medicalService));
        doNothing().when(medicalServiceRepository).delete(medicalService);

        assertDoesNotThrow(() -> medicalServiceService.delete(1));

        verify(medicalServiceRepository, times(1)).findById(1);
        verify(medicalServiceRepository, times(1)).delete(medicalService);
    }
}