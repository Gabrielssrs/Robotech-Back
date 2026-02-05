package com.example.demo.dto;

import com.example.demo.model.TorneoEstado;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class TorneoRequest {
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalTime horaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaInicioInscripcion;
    private Integer diasInscripcion; // 1, 3 o 5 días
    private LocalDate fechaLimiteInscripcion;
    private Long sedeId;
    private TorneoEstado estado;
    private List<Long> categoriaIds;
    private List<Long> juezIds;
}