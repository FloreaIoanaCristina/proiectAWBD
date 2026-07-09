package com.unibuc.management.mappers;

import com.unibuc.management.domain.Patient;
import com.unibuc.management.dto.response.PatientResponseDTO;
import com.unibuc.management.dto.summary.PatientSummaryDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PatientMapper {

    public static PatientResponseDTO toResponseDTO(Patient patient) {

        if (patient == null) {
            return null;
        }

        return PatientResponseDTO.builder()
                .id(patient.getId())
                .name(patient.getName())
                .medicalRecord(patient.getMedicalRecord())
                .subscription(patient.getSubscription())
                .age(patient.getAge())
                .sex(patient.getSex())
                .insuranceProvider(InsuranceProviderMapper.toSummary(patient.getInsuranceProvider()))
                .userId(
                        patient.getUser() != null
                                ? patient.getUser().getId()
                                : null
                )
                .build();
    }

    public static PatientSummaryDTO toSummary(Patient patient) {

        if (patient == null) {
            return null;
        }

        return PatientSummaryDTO.builder()
                .id(patient.getId())
                .name(patient.getName())
                .build();
    }
}