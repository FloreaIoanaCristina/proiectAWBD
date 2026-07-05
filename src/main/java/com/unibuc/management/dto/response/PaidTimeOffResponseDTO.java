package com.unibuc.management.dto.response;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaidTimeOffResponseDTO {

    private Integer id;

    private OffsetDateTime ptoFrom;

    private OffsetDateTime ptoTo;

    private Integer doctorId;
}
