package com.unibuc.management.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sent to medical-service to create the doctor profile for a new user.
 * Fields must match medical-service's InternalDoctorRequest.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalDoctorRequest {

    private String name;
    private String office;
    private Integer numberOfPtodays;
    private Integer medicalServiceId;
    private Long userId;
    private String username;
}
