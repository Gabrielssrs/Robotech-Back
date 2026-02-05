package com.example.demo.dto;

import lombok.Data;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

@Data
public class ClubUpdateRequest {
     @NotBlank(message = "El nombre del club es obligatorio")
    private String nombre;
    private String representante;
    private String telefono;
    private String slogan;
    private List<String> categorias;
    private String region;
    private String estado;
}