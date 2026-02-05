package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JuezProfileUpdateRequest {
    @NotBlank(message = "El nombre no puede estar vacío.")
    private String nombre;
    // Aquí podrías añadir otros campos que el juez pueda editar, como el teléfono.
}