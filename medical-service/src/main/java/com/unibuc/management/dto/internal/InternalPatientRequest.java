package com.unibuc.management.dto.internal;

import lombok.Data;

import java.time.LocalDate;

@Data
public class InternalPatientRequest {

    private String name;
    private LocalDate birthDate;
    private String medicalRecord;
    private Boolean sex;
    private Boolean subscription;
    private Integer insuranceProviderId;
    private Long userId;
    private String username;
}
