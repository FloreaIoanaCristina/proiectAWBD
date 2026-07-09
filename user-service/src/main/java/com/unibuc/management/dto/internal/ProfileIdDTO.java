package com.unibuc.management.dto.internal;

import lombok.*;

/**
 * Minimal profile reference (id) returned by medical-service.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileIdDTO {

    private Integer id;
}
