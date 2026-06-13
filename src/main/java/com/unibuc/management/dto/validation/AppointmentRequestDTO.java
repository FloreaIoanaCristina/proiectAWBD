package com.unibuc.management.dto.validation;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class AppointmentRequestDTO {

    private Integer patientId;

    @NotNull(message = "ID-ul serviciului medical este obligatoriu.")
    private Integer medicalServiceId;

    private Integer doctorId;

    @NotNull(message = "Data și ora programării sunt obligatorii.")
    @Future(message = "Programarea trebuie să fie stabilită într-o dată viitoare.")
    private OffsetDateTime appointmentFrom;

    @Pattern(regexp = "^(Appointed|Completed|Canceled)$",
            message = "Statusul programării poate fi doar: Appointed, Completed sau Canceled.")
    private String status;
}