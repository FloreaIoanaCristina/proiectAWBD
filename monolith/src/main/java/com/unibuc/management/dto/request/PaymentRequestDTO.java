package com.unibuc.management.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentRequestDTO {

    @NotNull(message = "Suma de plată este obligatorie.")
    @DecimalMin(value = "0.00", message = "Suma de plată nu poate fi negativă.")
    private Double amount;

    @Pattern(regexp = "^(Card|Cash)$", message = "Metoda de plată poate fi doar Card sau Cash.")
    private String paymentMethod;

    private LocalDate paymentDate;

    @NotNull(message = "ID-ul programării este obligatoriu.")
    private Integer appointmentId;

    @NotBlank(message = "Statusul tranzacției este obligatoriu.")
    @Pattern(regexp = "^(PENDING|COMPLETED)$", message = "Statusul poate fi doar PENDING sau COMPLETED.")
    private String status;
}
