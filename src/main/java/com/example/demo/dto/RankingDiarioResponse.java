package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingDiarioResponse {
    private LocalDate fecha;
    private String mensaje;
    private List<RankingCompetidorDto> topCompetidores;
    private List<RankingClubDto> topClubes;
}