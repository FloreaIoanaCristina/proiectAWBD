package com.unibuc.management.services;

import com.unibuc.management.domain.MedicalService;
import com.unibuc.management.dto.request.MedicalServiceRequestDTO;
import com.unibuc.management.dto.response.MedicalServiceResponseDTO;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.MedicalServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalServiceServiceTest {

    @InjectMocks
    private MedicalServiceService medicalServiceService;

    @Mock
    private MedicalServiceRepository medicalServiceRepository;

    private MedicalService medicalService;
    private MedicalServiceRequestDTO dto;

    @BeforeEach
    void setUp() {

        medicalService = new MedicalService();
        medicalService.setId(1);
        medicalService.setName("Consult");
        medicalService.setSpecialization("Cardiology");
        medicalService.setStartHour(8);
        medicalService.setEndHour(16);
        medicalService.setPrice(250.0);
        medicalService.setRating(4.5);
        medicalService.setNrOfRatings(10);

        dto = new MedicalServiceRequestDTO();
        dto.setName("Consult");
        dto.setSpecialization("Cardiology");
        dto.setStartHour(8);
        dto.setEndHour(16);
        dto.setPrice(250.0);
    }

    @Test
    void getAllServices_shouldReturnList() {

        when(medicalServiceRepository.findAll())
                .thenReturn(List.of(medicalService));

        List<MedicalServiceResponseDTO> result = medicalServiceService.getAllServices();

        assertEquals(1, result.size());
    }

    @Test
    void getMedicalServiceById_shouldReturnService() {

        when(medicalServiceRepository.findById(1))
                .thenReturn(Optional.of(medicalService));

        MedicalService result = medicalServiceService.getMedicalServiceById(1);

        assertEquals(1, result.getId());
    }

    @Test
    void getMedicalServiceById_shouldThrow_whenNotFound() {

        when(medicalServiceRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> medicalServiceService.getMedicalServiceById(1));
    }

    @Test
    void saveFromDto_shouldCreateSuccessfully() {

        when(medicalServiceRepository.save(any(MedicalService.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MedicalServiceResponseDTO result = medicalServiceService.saveFromDto(dto);

        assertEquals("Consult", result.getName());
        assertEquals(0.0, result.getRating());
        assertEquals(0, result.getNrOfRatings());
    }

    @Test
    void saveFromDto_shouldThrow_whenInvalidHours() {

        dto.setStartHour(16);
        dto.setEndHour(8);

        assertThrows(InvalidActionException.class,
                () -> medicalServiceService.saveFromDto(dto));
    }

    @Test
    void update_shouldUpdateSuccessfully() {

        when(medicalServiceRepository.findById(1))
                .thenReturn(Optional.of(medicalService));

        when(medicalServiceRepository.save(any(MedicalService.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        dto.setName("Updated");

        MedicalServiceResponseDTO result = medicalServiceService.update(1, dto);

        assertEquals("Updated", result.getName());
    }

    @Test
    void update_shouldThrow_whenServiceNotFound() {

        when(medicalServiceRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> medicalServiceService.update(1, dto));
    }

    @Test
    void delete_shouldDeleteSuccessfully() {

        when(medicalServiceRepository.findById(1))
                .thenReturn(Optional.of(medicalService));

        medicalServiceService.delete(1);

        verify(medicalServiceRepository).delete(medicalService);
    }

}