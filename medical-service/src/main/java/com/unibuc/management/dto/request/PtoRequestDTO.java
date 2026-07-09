package com.unibuc.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PtoRequestDTO {

    @NotNull(message = "ID-ul doctorului este obligatoriu.")
    private Integer doctorId;

    @NotNull(message = "Data de început este obligatorie.")
    private LocalDate startDate;

    @NotNull(message = "Data de sfârșit este obligatorie.")
    private LocalDate endDate;
}
