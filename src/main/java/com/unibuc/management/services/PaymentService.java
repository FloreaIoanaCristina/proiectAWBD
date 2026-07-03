package com.unibuc.management.services;

import com.unibuc.management.dto.validation.PaymentRequestDTO;
import com.unibuc.management.entities.Appointment;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.entities.Payment;
import com.unibuc.management.entities.ServiceCoverage;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PaymentService {

    private final PatientRepository patientRepository;
    private final MedicalServiceRepository medicalServiceRepository;
    private final ServiceCoverageRepository serviceCoverageRepository;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    public PaymentService(PatientRepository patientRepository,
                          MedicalServiceRepository medicalServiceRepository,
                          ServiceCoverageRepository serviceCoverageRepository,
                          PaymentRepository paymentRepository,
                          AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.medicalServiceRepository = medicalServiceRepository;
        this.serviceCoverageRepository = serviceCoverageRepository;
        this.paymentRepository = paymentRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public Payment createPaymentForPatient(Patient patient, MedicalService service, Appointment appointment) {
        log.debug("Inițiere calcul plată în așteptare pentru pacientul ID: {}, serviciul medical ID: {}", patient.getId(), service.getId());

        double price = service.getPrice();
        log.debug("Prețul de bază al serviciului '{}' este: {} RON", service.getName(), price);

        if (Boolean.TRUE.equals(patient.getSubscription())) {
            price = 0.0;
            log.info("Abonament activ detectat pentru pacientul ID: {}. Prețul a fost redus la 0 RON.", patient.getId());
        }
        else if (patient.getInsuranceProvider() != null) {
            Optional<ServiceCoverage> coverageOpt =
                    serviceCoverageRepository.findByMedicalServiceIdAndInsuranceProviderId(
                            service.getId(),
                            patient.getInsuranceProvider().getId()
                    );

            if (coverageOpt.isPresent()) {
                int percent = coverageOpt.get().getCoveragePercent();
                double oldPrice = price;
                price = price * (100 - percent) / 100;
                log.info("Acoperire asigurare aplicată: {}%. Prețul a fost redus de la {} RON la {} RON.",
                        percent, oldPrice, price);
            } else {
                log.warn("Serviciul medical ID {} nu este acoperit de asiguratorul '{}'. Se va aplica prețul integral.",
                        service.getId(), patient.getInsuranceProvider().getName());
            }
        } else {
            log.debug("Pacientul nu deține abonament sau asigurare. Se aplică prețul integral: {} RON", price);
        }

        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(price));

        payment.setPaymentMethod(null);
        payment.setPaymentDate(null);
        payment.setStatus("PENDING");
        payment.setAppointment(appointment);

        log.debug("Entitatea Payment a fost generată cu status PENDING și suma: {} RON.", price);
        return payment;
    }
    public List<Payment> getAllPayments() {
        log.debug("Se preia lista completă a plăților.");
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        log.debug("Căutare plată după ID: {}", id);
        return paymentRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Eroare interogare: Plata cu ID-ul {} nu a fost găsită în sistem.", id);
                return new ResourceNotFoundException("Plata cu ID-ul " + id + " nu a fost găsită.");
            });
    }

    public List<Payment> getPaymentsByPatientId(Long patientId) {
        log.debug("Se preia istoricul plăților pentru pacientul cu ID-ul: {}", patientId);
        return paymentRepository.findByPatientId(patientId);
    }
    @Transactional
    public Payment savePayment(PaymentRequestDTO dto) {
        log.info("Se înregistrează o plată nouă din DTO pentru suma: {}", dto.getAmount());

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Programarea cu ID-ul " + dto.getAppointmentId() + " nu există."));

        Payment payment = new Payment();

        payment.setAmount(BigDecimal.valueOf(dto.getAmount()));
        payment.setPaymentMethod(dto.getPaymentMethod());

        if (dto.getPaymentDate() != null) {
            payment.setPaymentDate(dto.getPaymentDate().atStartOfDay());
        } else {
            payment.setPaymentDate(LocalDateTime.now());
        }

        payment.setAppointment(appointment);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Plata a fost înregistrată cu succes (ID alocat: {}, Sumă: {} RON)",
                savedPayment.getId(), savedPayment.getAmount());
        return savedPayment;
    }

    @Transactional
    public Payment updatePayment(Long id, PaymentRequestDTO dto) {
        log.debug("Se solicită modificarea plății cu ID-ul: {}", id);

        Payment existingPayment = getPaymentById(id);

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Programarea cu ID-ul " + dto.getAppointmentId() + " nu există."));

        log.debug("Modificare plată ID {}: Status vechi: {} -> Status nou: {}, Sumă veche: {} RON -> Sumă nouă: {} RON",
                id, existingPayment.getStatus(), dto.getStatus(), existingPayment.getAmount(), dto.getAmount());

        existingPayment.setAmount(BigDecimal.valueOf(dto.getAmount()));
        existingPayment.setPaymentMethod(dto.getPaymentMethod());
        existingPayment.setAppointment(appointment);
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
        return updatedPayment;
    }

    @Transactional
    public void deletePayment(Long id) {
        log.debug("Se inițiază ștergerea plății cu ID-ul: {}", id);
        Payment existingPayment = getPaymentById(id);

        paymentRepository.delete(existingPayment);
        log.info("Înregistrarea plății cu ID-ul {} a fost eliminată definitiv din baza de date.", id);
    }
}