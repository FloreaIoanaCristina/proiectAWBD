package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;


@Entity
@Table(name = "PATIENT")
@Access(AccessType.FIELD)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "MEDICAL_RECORD", length = 4000)
    private String medicalRecord;

    @Column(name = "SUBSCRIPTION", nullable = false)
    private Boolean subscription;

    @Column(name = "AGE", nullable = false)
    private LocalDate age;

    @Column(name = "SEX", nullable = false)
    private Boolean sex;

    @OneToMany(mappedBy = "patient")
    private Set<Appointment> patientAppointments;

    @ManyToOne
    @JoinColumn(name = "INSURANCE_PROVIDER_ID")
    private InsuranceProvider insuranceProvider;

    @OneToOne
    @JoinColumn(name = "USER_ID", nullable = true)
    private User user;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(final String medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public Boolean getSubscription() {
        return subscription;
    }

    public void setSubscription(final Boolean subscription) {
        this.subscription = subscription;
    }

    public LocalDate getAge() {
        return age;
    }

    public void setAge(final LocalDate age) {
        this.age = age;
    }

    public Boolean getSex() {
        return sex;
    }

    public void setSex(final Boolean sex) {
        this.sex = sex;
    }

    public Set<Appointment> getPatientAppointments() {
        return patientAppointments;
    }

    public void setPatientAppointments(final Set<Appointment> patientAppointments) {
        this.patientAppointments = patientAppointments;
    }

    public InsuranceProvider getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(InsuranceProvider insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
