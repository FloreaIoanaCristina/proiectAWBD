package com.unibuc.management.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"medicalService", "insuranceProvider"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "SERVICE_INSURANCE_COVERAGE")
public class ServiceCoverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MEDICAL_SERVICE", nullable = false)
    private MedicalService medicalService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_INSURANCE_PROVIDER", nullable = false)
    private InsuranceProvider insuranceProvider;

    @Column(name = "COVERAGE_PERCENT")
    private Integer coveragePercent;
}