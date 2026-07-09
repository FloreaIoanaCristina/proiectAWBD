package com.unibuc.management.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Sent to medical-service to create the patient profile for a new user.
 * Fields must match medical-service's InternalPatientRequest.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
