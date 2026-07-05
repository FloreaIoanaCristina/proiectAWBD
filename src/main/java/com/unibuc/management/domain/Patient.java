package com.unibuc.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"patientAppointments", "insuranceProvider", "user"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "PATIENT")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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
    @JsonIgnore
    private Set<Appointment> patientAppointments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INSURANCE_PROVIDER_ID")
    private InsuranceProvider insuranceProvider;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;
}