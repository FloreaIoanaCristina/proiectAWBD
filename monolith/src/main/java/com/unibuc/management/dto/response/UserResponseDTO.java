package com.unibuc.management.dto.response;

import com.unibuc.management.security.Role;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;

    private String username;

    private Role role;

    private boolean enabled;
}
