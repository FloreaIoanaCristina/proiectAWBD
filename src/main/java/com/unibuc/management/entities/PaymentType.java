package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "PAYMENT_TYPE")
@Access(AccessType.FIELD)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class PaymentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "PRICE", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "WITH_INSURANCE", nullable = false)
    private Boolean withInsurance;

    @Column(name = "WITH_SUBSCRIPTION", nullable = false)
    private Boolean withSubscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "ID_MEDICAL_SERVICE", nullable = false)
    private MedicalService medicalService;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public Boolean getWithInsurance() {
        return withInsurance;
    }

    public void setWithInsurance(final Boolean withInsurance) {
        this.withInsurance = withInsurance;
    }

    public Boolean getWithSubscription() {
        return withSubscription;
    }

    public void setWithSubscription(final Boolean withSubscription) {
        this.withSubscription = withSubscription;
    }

    public MedicalService getMedicalService() {
        return medicalService;
    }

    public void setMedicalService(final MedicalService medicalService) {
        this.medicalService = medicalService;
    }

}
