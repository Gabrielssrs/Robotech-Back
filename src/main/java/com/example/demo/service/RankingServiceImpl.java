package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.Club;
import com.example.demo.model.Competidor;
import com.example.demo.model.Encuentro;
import com.example.demo.model.Robot;
import com.example.demo.repository.ClubRepository;
import com.example.demo.repository.CompetidorRepository;
import com.example.demo.repository.EncuentroRepository;
import com.example.demo.repository.RobotRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RobotRepository robotRepository;
    private final ClubRepository clubRepository;
    private final CompetidorRepository competidorRepository;
    private final EncuentroRepository encuentroRepository;

    @Override
    @Transactional(readOnly = true)
    public RankingGlobalResponse getRankingGlobal() {
        RankingGlobalResponse response = new RankingGlobalResponse();
        List<Encuentro> todosEncuentros = encuentroRepository.findAll();

        // 1. Calcular Ranking de Robots
        List<RankingRobotDto> robotsStats = calcularStatsRobots(todosEncuentros);
        
        // Ordenar por puntos para Top y Bottom
        robotsStats.sort(Comparator.comparing(RankingRobotDto::getPuntosTotales).reversed());
        response.setTopRobotsPuntos(robotsStats.stream().limit(5).collect(Collectors.toList()));
        
        List<RankingRobotDto> bottomRobots = new ArrayList<>(robotsStats);
        Collections.reverse(bottomRobots);
        response.setBottomRobotsPuntos(bottomRobots.stream().limit(5).collect(Collectors.toList()));

        // Ordenar por victorias
        robotsStats.sort(Comparator.comparing(RankingRobotDto::getTasaVictorias).reversed());
        response.setTopRobotsVictorias(robotsStats.stream().limit(5).collect(Collectors.toList()));

        if (!response.getTopRobotsPuntos().isEmpty()) {
            response.setMejorRobot(response.getTopRobotsPuntos().get(0));
        }

        // 2. Calcular Ranking de Clubes
        List<RankingClubDto> clubesStats = calcularStatsClubes(robotsStats);
        
        clubesStats.sort(Comparator.comparing(RankingClubDto::getPuntosTotales).reversed());
        response.setTopClubesPuntos(clubesStats.stream().limit(5).collect(Collectors.toList()));
        
        List<RankingClubDto> bottomClubes = new ArrayList<>(clubesStats);
        Collections.reverse(bottomClubes);
        response.setBottomClubesPuntos(bottomClubes.stream().limit(5).collect(Collectors.toList()));

        clubesStats.sort(Comparator.comparing(RankingClubDto::getTasaVictoriasPromedio).reversed());
        response.setTopClubesVictorias(clubesStats.stream().limit(5).collect(Collectors.toList()));

        if (!response.getTopClubesPuntos().isEmpty()) {
            response.setMejorClub(response.getTopClubesPuntos().get(0));
        }

        // 3. Calcular Ranking de Competidores
        List<RankingCompetidorDto> competidoresStats = calcularStatsCompetidores(robotsStats);
        competidoresStats.sort(Comparator.comparing(RankingCompetidorDto::getPuntosTotales).reversed());
        response.setTopCompetidoresPuntos(competidoresStats.stream().limit(5).collect(Collectors.toList()));

        if (!response.getTopCompetidoresPuntos().isEmpty()) {
            response.setMejorCompetidor(response.getTopCompetidoresPuntos().get(0));
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public RankingDiarioResponse getRankingDiario() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(23, 59, 59);

        // Buscar encuentros de hoy
        List<Encuentro> encuentrosDia = encuentroRepository.findByFechaEncuentroBetween(inicioDia, finDia);
        String mensaje = "Ranking del día " + hoy;

        // Si no hay encuentros hoy, buscar el último día con actividad
        if (encuentrosDia.isEmpty()) {
            Encuentro ultimoEncuentro = encuentroRepository.findTopByOrderByFechaEncuentroDesc();
            if (ultimoEncuentro != null) {
                LocalDate ultimaFecha = ultimoEncuentro.getFechaEncuentro().toLocalDate();
                inicioDia = ultimaFecha.atStartOfDay();
                finDia = ultimaFecha.atTime(23, 59, 59);
                encuentrosDia = encuentroRepository.findByFechaEncuentroBetween(inicioDia, finDia);
                mensaje = "Sin actividad hoy. Mostrando datos del " + ultimaFecha;
            } else {
                return new RankingDiarioResponse(hoy, "No hay datos históricos disponibles.", Collections.emptyList(), Collections.emptyList());
            }
        }

        List<RankingRobotDto> robotsStatsDia = calcularStatsRobots(encuentrosDia);
        robotsStatsDia = robotsStatsDia.stream()
                .filter(r -> r.getPuntosTotales() > 0 || r.getTasaVictorias() > 0)
                .collect(Collectors.toList());

        List<RankingCompetidorDto> competidoresDia = calcularStatsCompetidores(robotsStatsDia);
        competidoresDia.sort(Comparator.comparing(RankingCompetidorDto::getPuntosTotales).reversed());
        
        List<RankingClubDto> clubesDia = calcularStatsClubes(robotsStatsDia);
        clubesDia.sort(Comparator.comparing(RankingClubDto::getPuntosTotales).reversed());

        return new RankingDiarioResponse(inicioDia.toLocalDate(), mensaje,
                competidoresDia.stream().limit(5).collect(Collectors.toList()),
                clubesDia.stream().limit(5).collect(Collectors.toList()));
    }

    private List<RankingRobotDto> calcularStatsRobots(List<Encuentro> encuentros) {
        List<Robot> robots = robotRepository.findAll();
        return robots.stream().map(robot -> {
            double puntosEncuentros = encuentros.stream()
                    .mapToDouble(e -> {
                        if (e.getRobotA().getId().equals(robot.getId()) && e.getPuntosRobotA() != null) return e.getPuntosRobotA();
                        if (e.getRobotB().getId().equals(robot.getId()) && e.getPuntosRobotB() != null) return e.getPuntosRobotB();
                        return 0.0;
                    }).sum();
            
            long totalPartidos = encuentros.stream()
                    .filter(e -> (e.getRobotA().getId().equals(robot.getId()) || e.getRobotB().getId().equals(robot.getId())) && e.getRobotGanador() != null)
                    .count();
            long victorias = encuentros.stream()
                    .filter(e -> e.getRobotGanador() != null && e.getRobotGanador().getId().equals(robot.getId()))
                    .count();
            
            double tasa = totalPartidos > 0 ? (double) victorias / totalPartidos * 100 : 0.0;

            return new RankingRobotDto(
                    robot.getId(),
                    robot.getNombre(),
                    robot.getCategoria().getNombre(),
                    Math.round(puntosEncuentros * 10.0) / 10.0,
                    Math.round(tasa * 10.0) / 10.0,
                    robot.getClub().getNombre()
            );
        }).collect(Collectors.toList());
    }

    private List<RankingClubDto> calcularStatsClubes(List<RankingRobotDto> robotsStats) {
        List<Club> clubes = clubRepository.findAll();
        return clubes.stream().map(club -> {
            List<RankingRobotDto> robotsDelClub = robotsStats.stream()
                    .filter(r -> r.getClubNombre().equals(club.getNombre()))
                    .collect(Collectors.toList());
            
            double puntosTotales = robotsDelClub.stream().mapToDouble(RankingRobotDto::getPuntosTotales).sum();
            double tasaPromedio = robotsDelClub.stream().mapToDouble(RankingRobotDto::getTasaVictorias).average().orElse(0.0);

            return new RankingClubDto(club.getId(), club.getNombre(), robotsDelClub.size(), Math.round(tasaPromedio * 10.0) / 10.0, Math.round(puntosTotales * 10.0) / 10.0, club.getRegion(), club.getFotoUrl());
        }).collect(Collectors.toList());
    }

    private List<RankingCompetidorDto> calcularStatsCompetidores(List<RankingRobotDto> robotsStats) {
        List<Competidor> competidores = competidorRepository.findAll();
        return competidores.stream().map(comp -> {
            List<Robot> robotsDelCompetidor = robotRepository.findByCompetidor(comp);
            double puntosTotales = robotsDelCompetidor.stream().mapToDouble(r -> robotsStats.stream().filter(dto -> dto.getId().equals(r.getId())).mapToDouble(RankingRobotDto::getPuntosTotales).findFirst().orElse(0.0)).sum();
            return new RankingCompetidorDto(comp.getId(), comp.getNombre() + " " + comp.getApellido(), comp.getClub().getNombre(), 0, Math.round(puntosTotales * 10.0) / 10.0, comp.getFotoUrl());
        }).collect(Collectors.toList());
    }
}