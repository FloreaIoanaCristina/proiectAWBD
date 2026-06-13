package com.unibuc.management.dto.validation;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentRequestDTO {

    @NotNull(message = "Suma de plată este obligatorie.")
    @DecimalMin(value = "0.01", message = "Suma de plată trebuie să fie strict mai mare decât 0.")
    private Double amount;

    @NotBlank(message = "Metoda de plată este obligatorie.")
    @Pattern(regexp = "^(Card|Cash)$", message = "Metoda de plată poate fi doar Card sau Cash")
    private String paymentMethod;

    @NotNull(message = "Data plății este obligatorie.")
    private LocalDate paymentDate;

    @NotNull(message = "ID-ul programării este obligatoriu.")
    private Integer appointmentId;

    @NotNull(message = "ID-ul tipului de plată este obligatoriu.")
    private Integer paymentTypeId;
}
