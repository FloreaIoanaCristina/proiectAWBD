package com.unibuc.management.mappers;

import com.unibuc.management.domain.User;
import com.unibuc.management.dto.response.UserResponseDTO;
import com.unibuc.management.dto.summary.UserSummaryDTO;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

@UtilityClass
public class UserMapper {

    public static UserResponseDTO toResponseDTO(User user) {

        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }

    public static UserSummaryDTO toSummary(User user) {

        if (user == null) {
            return null;
        }

        return UserSummaryDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
