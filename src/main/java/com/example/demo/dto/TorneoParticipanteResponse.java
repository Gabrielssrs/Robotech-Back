package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TorneoParticipanteResponse {
    private Long id;
    private String nombreRobot;
    private String nombreCompetidor;
    private Double puntos;
    private String estado;
    private String fotoUrl;
}
