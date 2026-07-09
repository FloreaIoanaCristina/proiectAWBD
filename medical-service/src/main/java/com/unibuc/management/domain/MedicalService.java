package com.unibuc.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {
        "medicalServiceDoctors",
        "medicalServiceAppointments",
        "coveredByInsurances"
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "MEDICAL_SERVICE")
public class MedicalService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "medicalService"})
    private Set<Doctor> medicalServiceDoctors = new HashSet<>();

    @OneToMany(mappedBy = "medicalService")
    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "medicalService"})
    private Set<Appointment> medicalServiceAppointments = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "SERVICE_INSURANCE_COVERAGE",
            joinColumns = @JoinColumn(name = "ID_MEDICAL_SERVICE"),
            inverseJoinColumns = @JoinColumn(name = "ID_INSURANCE_PROVIDER")
    )
    private Set<InsuranceProvider> coveredByInsurances = new HashSet<>();
}