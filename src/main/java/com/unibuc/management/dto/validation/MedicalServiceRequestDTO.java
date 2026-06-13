package com.unibuc.management.dto.validation;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MedicalServiceRequestDTO {

    @NotBlank(message = "Numele serviciului medical este obligatoriu.")
    @Size(min = 3, max = 100, message = "Numele serviciului medical trebuie să aibă între 3 și 100 de caractere.")
    private String name;

    @NotBlank(message = "Specializarea este obligatorie.")
    @Size(min = 3, max = 100, message = "Specializarea trebuie să aibă între 3 și 100 de caractere.")
    private String specialization;

    @NotNull(message = "Ora de început este obligatorie.")
    @Min(value = 0, message = "Ora de început nu poate fi mai mică de 0.")
    @Max(value = 23, message = "Ora de început nu poate fi mai mare de 23.")
    private Integer startHour;

    @NotNull(message = "Ora de sfârșit este obligatorie.")
    @Min(value = 0, message = "Ora de sfârșit nu poate fi mai mică de 0.")
    @Max(value = 23, message = "Ora de sfârșit nu poate fi mai mare de 23.")
    private Integer endHour;

    @NotNull(message = "Prețul este obligatoriu.")
    @PositiveOrZero(message = "Prețul serviciului medical nu poate fi negativ.")
    private Double price;
}