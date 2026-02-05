package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompetidorRegistroRequest {
    @NotEmpty private String nombre;
    @NotEmpty private String apellido;
    @NotEmpty @Size(min = 8, max = 8) private String dni;
    @NotEmpty private String telefono;
    @NotEmpty @Email private String correoElectronico;
    @NotEmpty private String direccion;
    private String alias;
    private String categoria;
    @NotEmpty @Size(min = 8) private String contrasena;
    @NotEmpty private String codigo; // El código de un solo uso
}