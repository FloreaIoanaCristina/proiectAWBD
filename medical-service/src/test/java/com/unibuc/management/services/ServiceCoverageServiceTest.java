package com.unibuc.management.services;

import com.unibuc.management.domain.InsuranceProvider;
import com.unibuc.management.domain.MedicalService;
import com.unibuc.management.domain.ServiceCoverage;
import com.unibuc.management.dto.request.ServiceCoverageRequestDTO;
import com.unibuc.management.dto.response.ServiceCoverageResponseDTO;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.InsuranceProviderRepository;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.ServiceCoverageRepository;
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
class ServiceCoverageServiceTest {

    @InjectMocks
    private ServiceCoverageService serviceCoverageService;

    @Mock
    private ServiceCoverageRepository serviceCoverageRepository;

    @Mock
    private InsuranceProviderRepository insuranceProviderRepository;

    @Mock
    private MedicalServiceRepository medicalServiceRepository;

    private ServiceCoverage coverage;
    private ServiceCoverageRequestDTO dto;
    private MedicalService medicalService;
    private InsuranceProvider provider;

    @BeforeEach
    void setUp() {

        medicalService = new MedicalService();
        medicalService.setId(1);

        provider = new InsuranceProvider();
        provider.setId(1);

        coverage = new ServiceCoverage();
        coverage.setId(1L);
        coverage.setCoveragePercent(80);
        coverage.setMedicalService(medicalService);
        coverage.setInsuranceProvider(provider);

        dto = new ServiceCoverageRequestDTO();
        dto.setCoveragePercent(80);
        dto.setMedicalServiceId(1);
        dto.setInsuranceProviderId(1);
    }

    @Test
    void getCoverageById_shouldReturnCoverage() {

        when(serviceCoverageRepository.findById(1L))
                .thenReturn(Optional.of(coverage));

        ServiceCoverage result =
                serviceCoverageService.getCoverageById(1L);

        assertEquals(80, result.getCoveragePercent());
    }

    @Test
    void getCoverageById_shouldThrow_whenNotFound() {

        when(serviceCoverageRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> serviceCoverageService.getCoverageById(1L));
    }

    @Test
    void save_shouldCreateCoverageSuccessfully() {

        when(medicalServiceRepository.findById(1))
                .thenReturn(Optional.of(medicalService));

        when(insuranceProviderRepository.findById(1))
                .thenReturn(Optional.of(provider));

        when(serviceCoverageRepository.save(any(ServiceCoverage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServiceCoverageResponseDTO result = serviceCoverageService.save(dto);

        assertEquals(80, result.getCoveragePercent());
        assertEquals(provider.getId(), result.getInsuranceProvider().getId());
        assertEquals(medicalService.getId(), result.getMedicalService().getId());
    }

    @Test
    void updateCoverage_shouldUpdateSuccessfully() {

        dto.setCoveragePercent(50);

        when(serviceCoverageRepository.findById(1L))
                .thenReturn(Optional.of(coverage));

        when(medicalServiceRepository.findById(1))
                .thenReturn(Optional.of(medicalService));

        when(insuranceProviderRepository.findById(1))
                .thenReturn(Optional.of(provider));

        when(serviceCoverageRepository.save(any(ServiceCoverage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServiceCoverageResponseDTO result =
                serviceCoverageService.updateCoverage(1L, dto);

        assertEquals(50, result.getCoveragePercent());
    }

    @Test
    void deleteCoverage_shouldDeleteSuccessfully() {

        when(serviceCoverageRepository.findById(1L))
                .thenReturn(Optional.of(coverage));

        serviceCoverageService.deleteCoverage(1L);

        verify(serviceCoverageRepository).delete(coverage);
    }

    @Test
    void getAllCoverages_shouldReturnList() {

        when(serviceCoverageRepository.findAll())
                .thenReturn(List.of(coverage));

        List<ServiceCoverageResponseDTO> result =
                serviceCoverageService.getAllCoverages();

        assertEquals(1, result.size());
    }

}