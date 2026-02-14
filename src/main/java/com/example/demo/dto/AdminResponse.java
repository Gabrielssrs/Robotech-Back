package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "id", "nombre", "dni", "telefono", "correo", "rol", "estado" })
public class AdminResponse {
    // Asegúrate de que este campo exista y se esté poblando
    private Long id;
    private String nombre;
    private String dni;
    private String telefono;
    private String correo;
    private String rol;
    private String estado;
}