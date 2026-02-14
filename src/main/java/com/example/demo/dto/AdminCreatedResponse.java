package com.example.demo.dto;

import lombok.Data;

@Data
public class AdminCreatedResponse {
    private Long id;
    private String nombre;
    private String dni;
    private String telefono;
    private String correo;
    private String estado;
    private boolean isEnabled;
    private String rol;
}