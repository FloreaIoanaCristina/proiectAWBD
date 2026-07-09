package com.unibuc.management.mappers;

import com.unibuc.management.domain.PaidTimeOff;
import com.unibuc.management.dto.response.PaidTimeOffResponseDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PaidTimeOffMapper {

    public static PaidTimeOffResponseDTO toResponseDTO(PaidTimeOff paidTimeOff) {

        if (paidTimeOff == null) {
            return null;
        }

        return PaidTimeOffResponseDTO.builder()
                .id(paidTimeOff.getId())
                .from(paidTimeOff.getPtoFrom())
                .to(paidTimeOff.getPtoTo())
                .doctorId(
                        paidTimeOff.getDoctor() != null
                                ? paidTimeOff.getDoctor().getId()
                                : null
                )
                .build();
    }
}
