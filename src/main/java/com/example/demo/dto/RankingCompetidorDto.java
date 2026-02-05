package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingCompetidorDto {
    private Long id;
    private String nombreCompleto;
    private String clubNombre;
    private int rachaVictorias;
    private Double puntosTotales;
    private String fotoUrl;
}