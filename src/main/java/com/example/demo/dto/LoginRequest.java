package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "El correo electrónico no puede estar vacío")
    private String correoElectronico;
    @NotBlank(message = "La contraseña no puede estar vacía")
    private String contrasena;
}