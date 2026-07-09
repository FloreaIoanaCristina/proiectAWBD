package com.unibuc.management.mappers;

import com.unibuc.management.domain.Doctor;
import com.unibuc.management.dto.response.DoctorResponseDTO;
import com.unibuc.management.dto.summary.DoctorSummaryDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DoctorMapper {

    public static DoctorResponseDTO toResponseDTO(Doctor doctor) {

        if (doctor == null) {
            return null;
        }

        return DoctorResponseDTO.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .office(doctor.getOffice())
                .numberOfPTOdays(doctor.getNumberOfPTOdays())
                .medicalService(MedicalServiceMapper.toSummary(doctor.getMedicalService()))
                .userId(doctor.getUserId())
                .build();
    }
    public static DoctorSummaryDTO toSummary(Doctor doctor) {
        if (doctor == null) {
            return null;
        }

        return DoctorSummaryDTO.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .office(doctor.getOffice())
                .build();
    }
}