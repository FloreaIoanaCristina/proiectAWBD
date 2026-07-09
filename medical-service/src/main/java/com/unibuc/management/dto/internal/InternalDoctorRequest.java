package com.unibuc.management.dto.internal;

import lombok.Data;

@Data
public class InternalDoctorRequest {

    private String name;
    private String office;
    private Integer numberOfPtodays;
    private Integer medicalServiceId;
    private Long userId;
    private String username;
}
