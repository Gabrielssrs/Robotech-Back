package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompetidorUpdateRequest {
    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;
    private String apellido;
    private String alias;
    @Pattern(regexp = "^9\\d{8}$", message = "El teléfono debe empezar con 9 y tener 9 dígitos.")
    private String telefono;
    @NotBlank(message = "El DNI no puede estar vacío")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener 8 dígitos numéricos.")
    private String dni;
}