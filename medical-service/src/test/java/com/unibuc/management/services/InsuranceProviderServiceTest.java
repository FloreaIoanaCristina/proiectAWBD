package com.unibuc.management.services;

import com.unibuc.management.domain.InsuranceProvider;
import com.unibuc.management.dto.request.InsuranceProviderRequestDTO;
import com.unibuc.management.dto.response.InsuranceProviderResponseDTO;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.InsuranceProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceProviderServiceTest {

    @InjectMocks
    private InsuranceProviderService insuranceProviderService;

    @Mock
    private InsuranceProviderRepository insuranceProviderRepository;

    private InsuranceProvider provider;
    private InsuranceProviderRequestDTO dto;

    @BeforeEach
    void setUp() {

        provider = new InsuranceProvider();
        provider.setId(1);
        provider.setName("Allianz");
        provider.setContactNumber("0712345678");

        dto = new InsuranceProviderRequestDTO();
        dto.setName("Allianz");
        dto.setContactNumber("0712345678");
    }

    @Test
    void getInsuranceProviderById_shouldReturnProvider() {

        when(insuranceProviderRepository.findById(1))
                .thenReturn(Optional.of(provider));

        InsuranceProvider result =
                insuranceProviderService.getInsuranceProviderById(1);

        assertEquals(1, result.getId());
    }

    @Test
    void getInsuranceProviderById_shouldThrow_whenNotFound() {

        when(insuranceProviderRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> insuranceProviderService.getInsuranceProviderById(1));
    }

    @Test
    void createInsuranceProvider_shouldCreateSuccessfully() {

        when(insuranceProviderRepository.save(any(InsuranceProvider.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InsuranceProviderResponseDTO result =
                insuranceProviderService.createInsuranceProvider(dto);

        assertEquals("Allianz", result.getName());
        assertEquals("0712345678", result.getContactNumber());
    }

    @Test
    void updateInsuranceProvider_shouldUpdateSuccessfully() {

        dto.setName("NN");

        when(insuranceProviderRepository.findById(1))
                .thenReturn(Optional.of(provider));

        when(insuranceProviderRepository.save(any(InsuranceProvider.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InsuranceProviderResponseDTO result =
                insuranceProviderService.updateInsuranceProvider(1, dto);

        assertEquals("NN", result.getName());
    }

    @Test
    void deleteInsuranceProvider_shouldDeleteSuccessfully() {

        when(insuranceProviderRepository.findById(1))
                .thenReturn(Optional.of(provider));

        insuranceProviderService.deleteInsuranceProvider(1);

        verify(insuranceProviderRepository).delete(provider);
    }

    @Test
    void getAllInsuranceProvidersPaged_shouldReturnPage() {

        Page<InsuranceProvider> page =
                new PageImpl<>(List.of(provider));

        when(insuranceProviderRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<InsuranceProviderResponseDTO> result =
                insuranceProviderService.getAllInsuranceProvidersPaged(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

}