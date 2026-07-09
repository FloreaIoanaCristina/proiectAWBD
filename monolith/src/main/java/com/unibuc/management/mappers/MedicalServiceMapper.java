package com.unibuc.management.mappers;

import com.unibuc.management.domain.MedicalService;
import com.unibuc.management.dto.response.MedicalServiceResponseDTO;
import com.unibuc.management.dto.summary.MedicalServiceSummaryDTO;
import lombok.experimental.UtilityClass;
import java.util.stream.Collectors;

@UtilityClass
public class MedicalServiceMapper {

    public static MedicalServiceResponseDTO toResponseDTO(
            MedicalService medicalService) {

        if (medicalService == null) {
            return null;
        }

        return MedicalServiceResponseDTO.builder()
                .id(medicalService.getId())
                .name(medicalService.getName())
                .specialization(medicalService.getSpecialization())
                .startHour(medicalService.getStartHour())
                .endHour(medicalService.getEndHour())
                .price(medicalService.getPrice())
                .rating(medicalService.getRating())
                .nrOfRatings(medicalService.getNrOfRatings())
                .medicalServiceDoctors(
                medicalService.getMedicalServiceDoctors()
                        .stream()
                        .map(DoctorMapper::toSummary)
                        .collect(Collectors.toSet())
                )

                .coveredByInsurances(
                medicalService.getCoveredByInsurances()
                        .stream()
                        .map(InsuranceProviderMapper::toSummary)
                        .collect(Collectors.toSet())
                )
                .build();
    }

    public static MedicalServiceSummaryDTO toSummary(MedicalService service) {

        if (service == null) {
            return null;
        }

        return MedicalServiceSummaryDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .specialization(service.getSpecialization())
                .price(service.getPrice())
                .build();
    }
}