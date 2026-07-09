package com.unibuc.management.services;

import com.unibuc.management.clients.MedicalClient;
import com.unibuc.management.dto.internal.PricingInfoDTO;
import com.unibuc.management.dto.request.PaymentRequestDTO;
import com.unibuc.management.domain.Payment;
import com.unibuc.management.dto.response.PaymentResponseDTO;
import com.unibuc.management.dto.summary.AppointmentSummaryDTO;
import com.unibuc.management.dto.summary.PaymentSummaryDTO;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.mappers.PaymentMapper;
import com.unibuc.management.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MedicalClient medicalClient;

    @Transactional
    public PaymentSummaryDTO createPendingPayment(Integer appointmentId, Integer patientId) {
        log.debug("Generare plată PENDING pentru programarea ID: {} (pacient {})", appointmentId, patientId);

        PricingInfoDTO pricing = medicalClient.getPricingInfo(appointmentId);

        Payment payment = new Payment();
        payment.setAppointmentId(appointmentId);
        payment.setPatientId(patientId != null ? patientId : (pricing != null ? pricing.getPatientId() : null));
        payment.setPaymentMethod(null);
        payment.setPaymentDate(null);
        payment.setStatus("PENDING");
        payment.setAmount(computeAmount(pricing));

        Payment saved = paymentRepository.save(payment);
        log.info("Plata PENDING (ID {}) a fost creată pentru programarea {} cu suma {}.",
                saved.getId(), appointmentId, saved.getAmount());
        return PaymentMapper.toSummary(saved);
    }

    private BigDecimal computeAmount(PricingInfoDTO pricing) {
        if (pricing == null) {
            log.warn("Informațiile de preț lipsesc (medical-service indisponibil). Suma rămâne nedeterminată.");
            return null;
        }
        double price = pricing.getPrice();
        if (pricing.isSubscription()) {
            price = 0.0;
        } else if (pricing.getCoveragePercent() != null) {
            price = price * (100 - pricing.getCoveragePercent()) / 100;
        }
        return BigDecimal.valueOf(price);
    }

    public PaymentSummaryDTO getSummaryByAppointment(Integer appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId)
                .map(PaymentMapper::toSummary)
                .orElse(null);
    }

    public List<PaymentResponseDTO> getAllPayments() {
        log.debug("Se preia lista completă a plăților.");
        return paymentRepository.findAll()
                .stream().map(this::toEnrichedResponse)
                .toList();
    }

    public Payment getPaymentById(Long id) {
        log.debug("Căutare plată după ID: {}", id);
        return paymentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Eroare interogare: Plata cu ID-ul {} nu a fost găsită în sistem.", id);
                    return new ResourceNotFoundException("Plata cu ID-ul " + id + " nu a fost găsită.");
                });
    }

    public PaymentResponseDTO getPaymentResponseById(Long id) {
        return toEnrichedResponse(getPaymentById(id));
    }

    public List<PaymentResponseDTO> getPaymentsByPatientId(Integer patientId) {
        log.debug("Se preia istoricul plăților pentru pacientul cu ID-ul: {}", patientId);
        return paymentRepository.findByPatientId(patientId)
                .stream().map(this::toEnrichedResponse)
                .toList();
    }

    @Transactional
    public PaymentResponseDTO savePayment(PaymentRequestDTO dto) {
        log.info("Se înregistrează o plată nouă din DTO pentru suma: {}", dto.getAmount());

        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(dto.getAmount()));
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setStatus(dto.getStatus());
        payment.setAppointmentId(dto.getAppointmentId());
        payment.setPatientId(resolvePatientId(dto.getAppointmentId()));

        if (dto.getPaymentDate() != null) {
            payment.setPaymentDate(dto.getPaymentDate().atStartOfDay());
        } else {
            payment.setPaymentDate(LocalDateTime.now());
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Plata a fost înregistrată cu succes (ID alocat: {}, Sumă: {} RON)",
                savedPayment.getId(), savedPayment.getAmount());
        return toEnrichedResponse(savedPayment);
    }

    @Transactional
    public PaymentResponseDTO updatePayment(Long id, PaymentRequestDTO dto) {
        log.debug("Se solicită modificarea plății cu ID-ul: {}", id);

        Payment existingPayment = getPaymentById(id);

        log.debug("Modificare plată ID {}: Status vechi: {} -> Status nou: {}, Sumă veche: {} RON -> Sumă nouă: {} RON",
                id, existingPayment.getStatus(), dto.getStatus(), existingPayment.getAmount(), dto.getAmount());

        existingPayment.setAmount(BigDecimal.valueOf(dto.getAmount()));
        existingPayment.setPaymentMethod(dto.getPaymentMethod());
        existingPayment.setAppointmentId(dto.getAppointmentId());
        existingPayment.setStatus(dto.getStatus());

        if ("COMPLETED".equalsIgnoreCase(dto.getStatus())) {
            if (existingPayment.getPaymentDate() == null) {
                existingPayment.setPaymentDate(LocalDateTime.now());
                log.debug("Plata a fost marcată ca COMPLETED. S-a generat automat data achitării.");
            }
        } else if ("PENDING".equalsIgnoreCase(dto.getStatus())) {
            existingPayment.setPaymentDate(null);
            log.debug("Plata a fost marcată ca PENDING. Data achitării a fost resetată.");
        }

        Payment updatedPayment = paymentRepository.save(existingPayment);
        log.info("Plata cu ID-ul {} a fost actualizată cu succes. Status curent: {}", id, updatedPayment.getStatus());
        return toEnrichedResponse(updatedPayment);
    }

    @Transactional
    public void deletePayment(Long id) {
        log.debug("Se inițiază ștergerea plății cu ID-ul: {}", id);
        Payment existingPayment = getPaymentById(id);

        paymentRepository.delete(existingPayment);
        log.info("Înregistrarea plății cu ID-ul {} a fost eliminată definitiv din baza de date.", id);
    }

    private Integer resolvePatientId(Integer appointmentId) {
        AppointmentSummaryDTO summary = medicalClient.getAppointmentSummary(appointmentId);
        return summary != null ? summary.getPatientId() : null;
    }

    private PaymentResponseDTO toEnrichedResponse(Payment payment) {
        PaymentResponseDTO dto = PaymentMapper.toResponseDTO(payment);
        if (dto != null && payment.getAppointmentId() != null) {
            dto.setAppointment(medicalClient.getAppointmentSummary(payment.getAppointmentId()));
        }
        return dto;
    }
}
