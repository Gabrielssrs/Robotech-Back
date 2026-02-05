package com.example.demo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdateRequest {
    @Size(min = 2, message = "El nombre debe tener al menos 2 caracteres")
    private String nombre;

    @Pattern(regexp = "\\d{8}", message = "El DNI debe contener exactamente 8 dígitos numéricos.")
    private String dni;

    @Pattern(regexp = "9\\d{8}", message = "El teléfono debe empezar con 9 y tener 9 dígitos numéricos.")
    private String telefono;
}