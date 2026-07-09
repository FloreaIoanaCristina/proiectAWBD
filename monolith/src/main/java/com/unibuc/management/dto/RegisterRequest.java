package com.unibuc.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Numele de utilizator nu poate fi gol.")
    @Size(min = 4, max = 20, message = "Numele de utilizator trebuie să aibă între 4 și 20 de caractere.")
    private String username;
    @NotBlank(message = "Parola nu poate fi goală.")
    @Size(min = 6, message = "Parola trebuie să aibă minimum 6 caractere.")
    private String password;
    @NotNull(message = "Rolul este obligatoriu (PATIENT / DOCTOR ).")
    private String role;
    private String fullName;
    private LocalDate age;
    private Boolean sex;
    private String office;
    private Integer numberOfPTOdays;
    private Integer medicalServiceId;
}