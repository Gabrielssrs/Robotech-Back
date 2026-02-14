package com.example.demo.repository;

import lombok.Data;

@Data
public class SolicitudRequest {
    private String nombreCompleto;
    private String correoElectronico;
    private Long clubId; // Usamos el ID del club
    private String descripcionSolicitud;
}