package com.unibuc.management.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequestDTO {

    @NotBlank(message = "Numele pacientului este obligatoriu.")
    @Size(min = 3, max = 100, message = "Numele trebuie să conțină între 3 și 100 de caractere.")
    private String name;

    @NotNull(message = "Data de naștere a pacientului este obligatorie.")
    @Past(message = "Data de naștere trebuie să fie o dată din trecut.")
    //18 years
    private LocalDate birthDate;
    private String medicalRecord;

    @NotNull(message = "Specificarea sexului este obligatorie (true pentru M, false pentru F).")
    private Boolean sex;

    @NotNull(message = "Specificarea stării abonamentului este obligatorie.")
    private Boolean subscription;

    private Integer insuranceProviderId;

    @JsonIgnore
    private Long userId;
}