package com.unibuc.management.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceCoverageRequestDTO {

    @NotNull(message = "Procentul de acoperire este obligatoriu.")
    @Min(value = 0, message = "Procentul de acoperire nu poate fi mai mic de 0%.")
    @Max(value = 100, message = "Procentul de acoperire nu poate depăși 100%.")
    private Integer coveragePercent;

    @NotNull(message = "ID-ul asigurătorului este obligatoriu.")
    private Integer insuranceProviderId;

    @NotNull(message = "ID-ul serviciului medical este obligatoriu.")
    private Integer medicalServiceId;
}