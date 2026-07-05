package com.unibuc.management.mappers;

import com.unibuc.management.domain.PaidTimeOff;
import com.unibuc.management.dto.response.PaidTimeOffResponseDTO;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

@UtilityClass
public class PaidTimeOffMapper {

    public static PaidTimeOffResponseDTO toResponseDTO(PaidTimeOff paidTimeOff) {

        if (paidTimeOff == null) {
            return null;
        }

        return PaidTimeOffResponseDTO.builder()
                .id(paidTimeOff.getId())
                .ptoFrom(paidTimeOff.getPtoFrom())
                .ptoTo(paidTimeOff.getPtoTo())
                .doctorId(
                        paidTimeOff.getDoctor() != null
                                ? paidTimeOff.getDoctor().getId()
                                : null
                )
                .build();
    }
}
