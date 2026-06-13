package com.unibuc.management.dto.validation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class DoctorRequestDTO {
    @NotBlank(message = "Numele doctorului este obligatoriu.")
    @Size(min = 2, max = 50, message = "Numele trebuie să aibă între 2 și 50 de caractere.")
    private String name;
    @NotBlank(message = "Specificarea cabinetului  este obligatorie.")
    private String office;

    @NotNull(message = "Numărul de zile de concediu (PTO) este obligatoriu.")
    @Min(value = 0, message = "Numărul de zile de concediu nu poate fi negativ.")
    @Max(value = 365, message = "Numărul de zile de concediu depășește limita legală anuală.")
    private Integer numberOfPtodays;

    @NotNull(message = "ID-ul serviciului medical asociat este obligatoriu.")
    private Integer medicalServiceId;

    @JsonIgnore
    private Long userId;
}