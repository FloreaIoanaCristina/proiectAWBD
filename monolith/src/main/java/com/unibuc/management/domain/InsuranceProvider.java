package com.unibuc.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "coveredServices")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "INSURANCE_PROVIDER")
public class InsuranceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "CONTACT_NUMBER", length = 20)
    private String contactNumber;

    @ManyToMany(mappedBy = "coveredByInsurances")
    @JsonIgnore
    private Set<MedicalService> coveredServices;
}