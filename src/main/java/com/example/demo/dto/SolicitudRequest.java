package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SolicitudRequest {
    private String nombreCompleto;
    private String correoElectronico;
    @NotNull(message = "El ID del club es obligatorio")
    private Long clubId;
    private String descripcionSolicitud;
}