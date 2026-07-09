package com.unibuc.management.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceProviderResponseDTO {

    private Integer id;
    private String name;
    private String contactNumber;
}
