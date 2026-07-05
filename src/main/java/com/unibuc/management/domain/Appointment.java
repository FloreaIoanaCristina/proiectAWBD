package com.unibuc.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"payment", "medicalService", "doctor", "patient"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "APPOINTMENT")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "ID", nullable = false, updatable = false)
    private Integer id;

    @NotNull
    @Column(name = "APPOINTMENT_FROM", nullable = false)
    private OffsetDateTime appointmentFrom;

    @NotBlank
    @Column(name = "STATUS", nullable = false, length = 50)
    private String status;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "appointment"})
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MEDICAL_SERVICE", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "medicalServiceAppointments"})
    private MedicalService medicalService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOCTOR")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PATIENT", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patientAppointments"})
    private Patient patient;
}