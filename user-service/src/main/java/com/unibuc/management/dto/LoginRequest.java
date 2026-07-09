package com.unibuc.management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Numele de utilizator este obligatoriu.")
    private String username;
    @NotBlank(message = "Parola este obligatorie.")
    private String password;
}