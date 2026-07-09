package com.unibuc.management.repositories;

import com.unibuc.management.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPatientId(Integer patientId);

    Optional<Payment> findByAppointmentId(Integer appointmentId);
}