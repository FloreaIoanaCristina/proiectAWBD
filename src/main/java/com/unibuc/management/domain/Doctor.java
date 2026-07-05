package com.unibuc.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"medicalService", "doctorPaidTimeOffs", "doctorAppointments", "user"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "DOCTOR")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "ID", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "OFFICE", nullable = false, length = 50)
    private String office;

    @Column(name = "NUMBER_OFPTODAYS", nullable = false)
    private Integer numberOfPTOdays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MEDICAL_SERVICE", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "medicalServiceDoctors"})
    private MedicalService medicalService;

    @OneToMany(mappedBy = "doctor")
    @JsonIgnore
    private Set<PaidTimeOff> doctorPaidTimeOffs;

    @OneToMany(mappedBy = "doctor")
    @JsonIgnore
    private Set<Appointment> doctorAppointments;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;
}