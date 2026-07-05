package com.unibuc.management.dto.summary;

import com.unibuc.management.security.Role;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDTO {

    private Long id;
    private String username;
    private Role role;
}
