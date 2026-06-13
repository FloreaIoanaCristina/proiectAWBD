package com.unibuc.management.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "INSURANCE_PROVIDER")
public class InsuranceProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "CONTACT_NUMBER", length = 20)
    private String contactNumber;

    @ManyToMany(mappedBy = "coveredByInsurances")
    @JsonIgnore
    private Set<MedicalService> coveredServices;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public Set<MedicalService> getCoveredServices() { return coveredServices; }
    public void setCoveredServices(Set<MedicalService> coveredServices) { this.coveredServices = coveredServices; }
}
