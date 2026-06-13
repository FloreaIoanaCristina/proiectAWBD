package com.unibuc.management.dto.validation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PtoRequestDTO {

    @NotNull(message = "ID-ul doctorului este obligatoriu.")
    private Integer doctorId;

    @NotNull(message = "Data de început a concediului este obligatorie.")
    @FutureOrPresent(message = "Data de început nu poate fi în trecut.")
    private OffsetDateTime startDate;

    @NotNull(message = "Data de sfârșit a concediului este obligatorie.")
    @FutureOrPresent(message = "Data de sfârșit nu poate fi în trecut.")
    private OffsetDateTime endDate;
}
