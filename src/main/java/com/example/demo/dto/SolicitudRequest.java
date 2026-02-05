package com.example.demo.dto;

import lombok.Data;

@Data
public class SolicitudRequest {
    private String nombreCompleto;
    private String correoElectronico;
    private Long clubId; // Usamos el ID del club
    private String descripcionSolicitud;
}