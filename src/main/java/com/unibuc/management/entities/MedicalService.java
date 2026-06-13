package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.util.Set;


@Entity
@Table(name = "MEDICAL_SERVICE")
@Access(AccessType.FIELD)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MedicalService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "SPECIALIZATION", nullable = false, length = 100)
    private String specialization;

    @Column(name = "START_HOUR", nullable = false)
    private Integer startHour;

    @Column(name = "END_HOUR", nullable = false)
    private Integer endHour;

    @Column(name = "PRICE", nullable = false)
    private Double price;

    @Column(name = "RATING", nullable = false)
    private Double rating;

    @Column(name = "NR_OF_RATINGS", nullable = false)
    private Integer nrOfRatings;

    @OneToMany(mappedBy = "medicalService")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patientAppointments"})
    private Set<Doctor> medicalServiceDoctors;

    @OneToMany(mappedBy = "medicalService")
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "patientAppointments"})
    private Set<Appointment> medicalServiceAppointments;

    @ManyToMany
    @JoinTable(
            name = "SERVICE_INSURANCE_COVERAGE",
            joinColumns = @JoinColumn(name = "ID_MEDICAL_SERVICE"),
            inverseJoinColumns = @JoinColumn(name = "ID_INSURANCE_PROVIDER")
    )
    private Set<InsuranceProvider> coveredByInsurances;

    @ManyToMany
    @JoinTable(
            name = "SERVICE_SUBSCRIPTION_PLAN",
            joinColumns = @JoinColumn(name = "ID_MEDICAL_SERVICE"),
            inverseJoinColumns = @JoinColumn(name = "ID_SUBSCRIPTION_PLAN")
    )
    private Set<SubscriptionPlan> includedInSubscriptions;

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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(final String specialization) {
        this.specialization = specialization;
    }

    public Integer getStartHour() {
        return startHour;
    }

    public void setStartHour(final Integer startHour) {
        this.startHour = startHour;
    }

    public Integer getEndHour() {
        return endHour;
    }

    public void setEndHour(final Integer endHour) {
        this.endHour = endHour;
    }
    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
    public Double getRating() {
        return rating;
    }

    public void setRating(final Double rating) {
        this.rating = rating;
    }

    public Integer getNrOfRatings() {
        return nrOfRatings;
    }

    public void setNrOfRatings(final Integer nrOfRatings) {
        this.nrOfRatings = nrOfRatings;
    }

    public Set<Doctor> getMedicalServiceDoctors() {
        return medicalServiceDoctors;
    }

    public void setMedicalServiceDoctors(final Set<Doctor> medicalServiceDoctors) {
        this.medicalServiceDoctors = medicalServiceDoctors;
    }

    public Set<Appointment> getMedicalServiceAppointments() {
        return medicalServiceAppointments;
    }

    public void setMedicalServiceAppointments(final Set<Appointment> medicalServiceAppointments) {
        this.medicalServiceAppointments = medicalServiceAppointments;
    }

    public Set<InsuranceProvider> getCoveredByInsurances() {
        return coveredByInsurances;
    }

    public void setCoveredByInsurances(Set<InsuranceProvider> coveredByInsurances) {
        this.coveredByInsurances = coveredByInsurances;
    }

    public Set<SubscriptionPlan> getIncludedInSubscriptions() {
        return includedInSubscriptions;
    }

    public void setIncludedInSubscriptions(Set<SubscriptionPlan> includedInSubscriptions) {
        this.includedInSubscriptions = includedInSubscriptions;
    }
}
