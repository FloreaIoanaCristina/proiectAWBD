package com.unibuc.management.services;

import com.unibuc.management.clients.MedicalClient;
import com.unibuc.management.domain.Payment;
import com.unibuc.management.dto.internal.PricingInfoDTO;
import com.unibuc.management.dto.request.PaymentRequestDTO;
import com.unibuc.management.dto.response.PaymentResponseDTO;
import com.unibuc.management.dto.summary.AppointmentSummaryDTO;
import com.unibuc.management.dto.summary.PaymentSummaryDTO;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private MedicalClient medicalClient;

    private PaymentRequestDTO dto;
    private Payment payment;

    @BeforeEach
    void setUp() {
        dto = new PaymentRequestDTO();
        dto.setAmount(200.0);
        dto.setAppointmentId(1);
        dto.setPaymentMethod("Card");
        dto.setStatus("PENDING");
        dto.setPaymentDate(LocalDate.now());

        payment = new Payment();
        payment.setId(1L);
        payment.setAmount(BigDecimal.valueOf(200));
        payment.setStatus("PENDING");
        payment.setAppointmentId(1);
        payment.setPatientId(1);
    }

    @Test
    void createPendingPayment_shouldBeFree_whenSubscriptionActive() {
        when(medicalClient.getPricingInfo(1)).thenReturn(
                PricingInfoDTO.builder().patientId(1).price(200.0).subscription(true).build());
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PaymentSummaryDTO result = paymentService.createPendingPayment(1, 1);

        assertEquals(0.0, result.getAmount().doubleValue());
    }

    @Test
    void createPendingPayment_shouldApplyInsuranceCoverage() {
        when(medicalClient.getPricingInfo(1)).thenReturn(
                PricingInfoDTO.builder().patientId(1).price(200.0).subscription(false).coveragePercent(50).build());
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PaymentSummaryDTO result = paymentService.createPendingPayment(1, 1);

        assertEquals(100.0, result.getAmount().doubleValue());
    }

    @Test
    void createPendingPayment_shouldApplyFullPrice_whenNoCoverage() {
        when(medicalClient.getPricingInfo(1)).thenReturn(
                PricingInfoDTO.builder().patientId(1).price(200.0).subscription(false).build());
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PaymentSummaryDTO result = paymentService.createPendingPayment(1, 1);

        assertEquals(200.0, result.getAmount().doubleValue());
    }

    @Test
    void savePayment_shouldSaveSuccessfully() {
        when(medicalClient.getAppointmentSummary(1))
                .thenReturn(AppointmentSummaryDTO.builder().id(1).patientId(1).build());
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PaymentResponseDTO result = paymentService.savePayment(dto);

        assertNotNull(result);
        assertEquals(200.0, result.getAmount().doubleValue());
    }

    @Test
    void updatePayment_shouldMarkCompleted_andSetDate() {
        dto.setStatus("COMPLETED");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PaymentResponseDTO result = paymentService.updatePayment(1L, dto);

        assertEquals("COMPLETED", result.getStatus());
        assertNotNull(result.getPaymentDate());
    }

    @Test
    void updatePayment_shouldResetDate_whenPending() {
        dto.setStatus("PENDING");
        payment.setPaymentDate(java.time.LocalDateTime.now());
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PaymentResponseDTO result = paymentService.updatePayment(1L, dto);

        assertEquals("PENDING", result.getStatus());
        assertNull(result.getPaymentDate());
    }
    @Test
    void getSummaryByAppointment_shouldReturnSummary() {
        when(paymentRepository.findByAppointmentId(1)).thenReturn(Optional.of(payment));

        PaymentSummaryDTO result = paymentService.getSummaryByAppointment(1);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getSummaryByAppointment_shouldReturnNull_whenMissing() {
        when(paymentRepository.findByAppointmentId(1)).thenReturn(Optional.empty());

        assertNull(paymentService.getSummaryByAppointment(1));
    }

    @Test
    void deletePayment_shouldDeleteSuccessfully() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        doNothing().when(paymentRepository).delete(payment);

        paymentService.deletePayment(1L);

        verify(paymentRepository).delete(payment);
    }

    @Test
    void deletePayment_shouldThrow_whenNotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.deletePayment(1L));
    }
}
