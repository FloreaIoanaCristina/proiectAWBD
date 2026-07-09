package com.unibuc.management.dto.response;

import com.unibuc.management.dto.summary.DoctorSummaryDTO;
import com.unibuc.management.dto.summary.MedicalServiceSummaryDTO;
import com.unibuc.management.dto.summary.PatientSummaryDTO;
import com.unibuc.management.dto.summary.PaymentSummaryDTO;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDTO {

    private Integer id;

    private OffsetDateTime appointmentFrom;

    private String status;

    private PaymentSummaryDTO payment;

    private MedicalServiceSummaryDTO medicalService;

    private DoctorSummaryDTO doctor;

    private PatientSummaryDTO patient;
}
