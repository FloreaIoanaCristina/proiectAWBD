package com.unibuc.management.services;
import com.unibuc.management.domain.*;
import com.unibuc.management.dto.request.PaymentRequestDTO;
import com.unibuc.management.dto.response.PaymentResponseDTO;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock private ServiceCoverageRepository serviceCoverageRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private AppointmentRepository appointmentRepository;

    private Patient patient;
    private MedicalService service;
    private Appointment appointment;
    private PaymentRequestDTO dto;
    private Payment payment;

    @BeforeEach
    void setUp() {

        patient = new Patient();
        patient.setId(1);
        patient.setSubscription(false);

        InsuranceProvider provider = new InsuranceProvider();
        provider.setId(10);
        provider.setName("Allianz");

        patient.setInsuranceProvider(provider);

        service = new MedicalService();
        service.setId(100);
        service.setName("Consult");
        service.setPrice(200.0);

        appointment = new Appointment();
        appointment.setId(1);

        dto = new PaymentRequestDTO();
        dto.setAmount(200.0);
        dto.setAppointmentId(1);
        dto.setPaymentMethod("Card");
        dto.setStatus("PENDING");
        dto.setPaymentDate(LocalDate.now());

        payment = new Payment();
        payment.setId(1L);
        payment.setAmount(java.math.BigDecimal.valueOf(200));
        payment.setStatus("PENDING");
        payment.setAppointment(appointment);
    }


    @Test
    void createPayment_shouldBeFree_whenSubscriptionActive() {

        patient.setSubscription(true);

        Payment result = paymentService.createPaymentForPatient(patient, service, appointment);

        assertEquals(0.0, result.getAmount().doubleValue());
    }

    @Test
    void createPayment_shouldApplyInsuranceCoverage() {

        ServiceCoverage coverage = new ServiceCoverage();
        coverage.setCoveragePercent(50);

        when(serviceCoverageRepository
                .findByMedicalServiceIdAndInsuranceProviderId(100, 10))
                .thenReturn(Optional.of(coverage));

        Payment result = paymentService.createPaymentForPatient(patient, service, appointment);

        assertEquals(100.0, result.getAmount().doubleValue());
    }

    @Test
    void createPayment_shouldApplyFullPrice_whenNoCoverage() {

        when(serviceCoverageRepository
                .findByMedicalServiceIdAndInsuranceProviderId(100, 10))
                .thenReturn(Optional.empty());

        Payment result = paymentService.createPaymentForPatient(patient, service, appointment);

        assertEquals(200.0, result.getAmount().doubleValue());
    }

    @Test
    void savePayment_shouldSaveSuccessfully() {

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        PaymentResponseDTO result = paymentService.savePayment(dto);

        assertNotNull(result);
        assertEquals(200.0, result.getAmount().doubleValue());
    }

    @Test
    void savePayment_shouldThrow_whenAppointmentNotFound() {

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.savePayment(dto));
    }

    @Test
    void updatePayment_shouldMarkCompleted_andSetDate() {

        dto.setStatus("COMPLETED");

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        PaymentResponseDTO result = paymentService.updatePayment(1L, dto);

        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getPaymentDate());
    }

    @Test
    void updatePayment_shouldResetDate_whenPending() {

        dto.setStatus("PENDING");

        payment.setPaymentDate(java.time.LocalDateTime.now());

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.of(appointment));

        when(paymentRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        PaymentResponseDTO result = paymentService.updatePayment(1L, dto);

        assertEquals("PENDING", result.getStatus());
        assertNull(result.getPaymentDate());
    }

    @Test
    void deletePayment_shouldDeleteSuccessfully() {

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.of(payment));

        doNothing().when(paymentRepository).delete(payment);

        paymentService.deletePayment(1L);

        verify(paymentRepository).delete(payment);
    }

    @Test
    void deletePayment_shouldThrow_whenNotFound() {

        when(paymentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.deletePayment(1L));
    }
}