package com.unibuc.management.dto.validation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InsuranceProviderRequestDTO {

    @NotBlank(message = "Numele companiei de asigurări este obligatoriu.")
    @Size(min = 2, max = 100, message = "Numele companiei trebuie să aibă între 2 și 100 de caractere.")
    private String name;

    @NotBlank(message = "Numărul de contact este obligatoriu.")
    @Pattern(regexp = "^[0-9+\\s-]{7,15}$", message = "Numărul de contact introdus nu este valid.")
    private String contactNumber;
}
