package com.unibuc.management.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "service_insurance_coverage")
public class ServiceCoverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private MedicalService medicalService;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private InsuranceProvider insuranceProvider;

    @Column(name = "coverage_percent")
    private Integer coveragePercent;

    public ServiceCoverage() {}

    public ServiceCoverage(MedicalService service, InsuranceProvider provider, Integer percent) {
        this.medicalService = service;
        this.insuranceProvider = provider;
        this.coveragePercent = percent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MedicalService getMedicalService() { return medicalService; }
    public void setMedicalService(MedicalService medicalService) { this.medicalService = medicalService; }

    public InsuranceProvider getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(InsuranceProvider insuranceProvider) { this.insuranceProvider = insuranceProvider; }

    public Integer getCoveragePercent() { return coveragePercent; }
    public void setCoveragePercent(Integer coveragePercent) { this.coveragePercent = coveragePercent; }
}