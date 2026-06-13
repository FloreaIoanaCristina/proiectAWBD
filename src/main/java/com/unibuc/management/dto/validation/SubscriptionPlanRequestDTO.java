package com.unibuc.management.dto.validation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionPlanRequestDTO {

    @NotBlank(message = "Numele planului de abonament este obligatoriu.")
    @Size(min = 3, max = 50, message = "Numele abonamentului trebuie să aibă între 3 și 50 de caractere.")
    private String name;

    @NotNull(message = "Taxa lunară (Monthly Fee) este obligatorie.")
    @DecimalMin(value = "0.00", message = "Taxa lunară nu poate fi negativă.")
    @Digits(integer = 8, fraction = 2, message = "Formatul prețului este invalid (maxim 8 cifre și 2 zecimale).")
    private BigDecimal monthlyFee;

    private String description;
}
