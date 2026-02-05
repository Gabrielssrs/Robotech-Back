package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingClubDto {
    private Long id;
    private String nombre;
    private int totalRobots;
    private Double tasaVictoriasPromedio;
    private Double puntosTotales;
    private String region;
    private String fotoUrl;
}