
package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompetidorRegistroClubRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    private String alias;
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
    private String dni;
    private String telefono;
    @Email(message = "Formato de correo inválido")
    private String correoElectronico;
    private String direccion;
    private String categoria;
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String contrasena;
}
    
