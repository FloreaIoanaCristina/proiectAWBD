package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "SUBSCRIPTION_PLAN")
@Access(AccessType.FIELD)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "MONTHLY_FEE", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyFee;

    @Column(name = "DESCRIPTION")
    private String description;

    @ManyToMany(mappedBy = "includedInSubscriptions")
    @JsonIgnore
    private Set<MedicalService> services;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(BigDecimal monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<MedicalService> getServices() {
        return services;
    }

    public void setServices(Set<MedicalService> services) {
        this.services = services;
    }
}

