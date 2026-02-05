package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class RankingGlobalResponse {
    private RankingRobotDto mejorRobot;
    private RankingClubDto mejorClub;
    private RankingCompetidorDto mejorCompetidor;

    private List<RankingRobotDto> topRobotsPuntos;
    private List<RankingClubDto> topClubesPuntos;
    private List<RankingCompetidorDto> topCompetidoresPuntos;

    private List<RankingRobotDto> topRobotsVictorias;
    private List<RankingClubDto> topClubesVictorias;

    private List<RankingRobotDto> bottomRobotsPuntos;
    private List<RankingClubDto> bottomClubesPuntos;
}