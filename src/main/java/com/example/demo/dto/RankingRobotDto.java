package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingRobotDto {
    private Long id;
    private String nombre;
    private String categoria;
    private Double puntosTotales;
    private Double tasaVictorias;
    private String clubNombre;
}