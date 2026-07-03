package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.time.OffsetDateTime;


@Entity
@Table(name = "APPOINTMENT")
@Access(AccessType.FIELD)
//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "APPOINTMENT_FROM", nullable = false)
    private OffsetDateTime appointmentFrom;

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status;
    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "appointment"})
    private Payment payment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_MEDICAL_SERVICE", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "medicalServiceAppointments"})
    private MedicalService medicalService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOCTOR", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Doctor doctor;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PATIENT", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patientAppointments"})
    private Patient patient;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public OffsetDateTime getAppointmentFrom() {
        return appointmentFrom;
    }

    public void setAppointmentFrom(final OffsetDateTime appointmentFrom) {
        this.appointmentFrom = appointmentFrom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public MedicalService getMedicalService() {
        return medicalService;
    }

    public void setMedicalService(final MedicalService medicalService) {
        this.medicalService = medicalService;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(final Patient patient) {
        this.patient = patient;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(final Payment payment) {
        this.payment = payment;
    }

    public Doctor getDoctor() {
        return doctor;
    }
    public void setDoctor(final Doctor doctor) {
        this.doctor = doctor;
    }
}
