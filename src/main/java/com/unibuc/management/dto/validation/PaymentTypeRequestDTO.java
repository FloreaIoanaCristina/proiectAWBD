package com.unibuc.management.dto.validation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentTypeRequestDTO {

    @NotNull(message = "Prețul este obligatoriu.")
    @DecimalMin(value = "0.00", message = "Prețul nu poate fi negativ.")
    @Digits(integer = 10, fraction = 2, message = "Prețul are un format zecimal invalid (maxim 10 cifre și 2 zecimale).")
    private BigDecimal price;

    @NotNull(message = "Trebuie specificat dacă include asigurare (true/false).")
    private Boolean withInsurance;

    @NotNull(message = "Trebuie specificat dacă include abonament (true/false).")
    private Boolean withSubscription;

    @NotNull(message = "ID-ul serviciului medical asociat este obligatoriu.")
    private Integer medicalServiceId;
}