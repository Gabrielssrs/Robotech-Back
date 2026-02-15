package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
public class TorneoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalTime horaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaInicioInscripcion;
    private Integer diasInscripcion;
    private String nombreSede;
    private Long sedeId;
    private boolean activo;
    private String estado;
    private List<String> categorias;
    private List<Long> categoriaIds;
    private List<Long> juezIds;
}
