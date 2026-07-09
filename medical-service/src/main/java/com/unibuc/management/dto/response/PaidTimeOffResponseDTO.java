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

    private OffsetDateTime from;

    private OffsetDateTime to;

    private Integer doctorId;
}
