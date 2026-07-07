package com.unibuc.management.dto.summary;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSummaryDTO {

    private Integer id;

    private OffsetDateTime appointmentFrom;

    private String status;

    private Integer patientId;
    private String patientName;

    private Integer doctorId;
    private String doctorName;

    private Integer medicalServiceId;
    private String medicalServiceName;
}
