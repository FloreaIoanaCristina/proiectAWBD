package com.unibuc.management.services;

import com.unibuc.management.entities.InsuranceProvider;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.entities.PaymentType;
import com.unibuc.management.repositories.PaymentTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentTypeServiceTest {

    @Mock
    private PaymentTypeRepository paymentTypeRepository;

    @Mock
    private MedicalServiceService medicalServiceService;

    @Mock
    private PatientService patientService;

    private PaymentTypeService paymentTypeService;

    private MedicalService medicalService;
    private Patient patient;
    private PaymentType paymentType;

    @BeforeEach
    void setUp() {
        paymentTypeService = new PaymentTypeService(paymentTypeRepository, medicalServiceService, patientService);

        medicalService = new MedicalService();
        medicalService.setId(1);
        medicalService.setName("General Checkup");

        patient = new Patient();
        patient.setId(1);
        patient.setName("John Doe");
        patient.setInsuranceProvider(new InsuranceProvider());
        patient.setSubscription(false);

        paymentType = new PaymentType();
        paymentType.setId(1);
        paymentType.setMedicalService(medicalService);
        paymentType.setWithInsurance(true);
        paymentType.setWithSubscription(false);
    }

    @Test
    void testGetPaymentTypeById_Found() {
        when(paymentTypeRepository.findById(1)).thenReturn(Optional.of(paymentType));

        PaymentType result = paymentTypeService.getPaymentTypeById(1);

        assertNotNull(result);
        assertEquals(paymentType.getId(), result.getId());
        verify(paymentTypeRepository, times(1)).findById(1);
    }

    @Test
    void testGetPaymentTypeById_NotFound() {
        when(paymentTypeRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentTypeService.getPaymentTypeById(1));
        verify(paymentTypeRepository, times(1)).findById(1);
    }

    @Test
    void testGetPriceForPatient_WithInsurance() {
        when(medicalServiceService.getMedicalServiceById(1)).thenReturn(medicalService);
        when(patientService.getPatientById(1)).thenReturn(patient);
        when(paymentTypeRepository.findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, true, false))
                .thenReturn(Optional.of(paymentType));

        PaymentType result = paymentTypeService.getPriceForPatient(1, 1);

        assertNotNull(result);
        assertEquals(paymentType.getId(), result.getId());
        verify(medicalServiceService, times(1)).getMedicalServiceById(1);
        verify(patientService, times(1)).getPatientById(1);
    }

    @Test
    void testGetPriceForPatient_WithSubscription() {
        patient.setInsuranceProvider(null);
        patient.setSubscription(true);

        when(medicalServiceService.getMedicalServiceById(1)).thenReturn(medicalService);
        when(patientService.getPatientById(1)).thenReturn(patient);
        when(paymentTypeRepository.findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, false, true))
                .thenReturn(Optional.of(paymentType));

        PaymentType result = paymentTypeService.getPriceForPatient(1, 1);

        assertNotNull(result);
        verify(paymentTypeRepository, times(1)).findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, false, true);
    }

    @Test
    void testGetPriceForPatient_NoInsuranceNoSubscription() {
        patient.setInsuranceProvider(null);
        patient.setSubscription(false);

        when(medicalServiceService.getMedicalServiceById(1)).thenReturn(medicalService);
        when(patientService.getPatientById(1)).thenReturn(patient);
        when(paymentTypeRepository.findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, false, false))
                .thenReturn(Optional.of(paymentType));

        PaymentType result = paymentTypeService.getPriceForPatient(1, 1);

        assertNotNull(result);
        verify(paymentTypeRepository, times(1)).findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, false, false);
    }

    @Test
    void testGetPriceForPatient_ServiceNotFound() {
        when(medicalServiceService.getMedicalServiceById(1)).thenThrow(new ResourceNotFoundException("Not found"));

        assertThrows(ResourceNotFoundException.class, () -> paymentTypeService.getPriceForPatient(1, 1));

        verify(medicalServiceService, times(1)).getMedicalServiceById(1);
        verify(patientService, never()).getPatientById(anyInt());
    }

    @Test
    void testGetPriceForPatient_NoPaymentTypeDefined() {
        patient.setInsuranceProvider(null);
        patient.setSubscription(false);

        when(medicalServiceService.getMedicalServiceById(1)).thenReturn(medicalService);
        when(patientService.getPatientById(1)).thenReturn(patient);
        when(paymentTypeRepository.findByMedicalServiceIdAndWithInsuranceAndWithSubscription(1, false, false))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentTypeService.getPriceForPatient(1, 1));
    }
}