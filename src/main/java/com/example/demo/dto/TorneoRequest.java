package com.example.demo.dto;

import com.example.demo.model.TorneoEstado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class TorneoRequest {
    @NotBlank(message = "El nombre del torneo es obligatorio")
    private String nombre;
    private String descripcion;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "La fecha de inicio de inscripciones es obligatoria")
    @FutureOrPresent(message = "La fecha de inscripción no puede ser en el pasado")
    private LocalDate fechaInicioInscripcion;

    @NotNull(message = "La duración de inscripción es obligatoria")
    private Integer diasInscripcion; // 1, 3 o 5 días
    private LocalDate fechaLimiteInscripcion;
    @NotNull(message = "La sede es obligatoria")
    private Long sedeId;
    private TorneoEstado estado;
    @NotEmpty(message = "Debe seleccionar al menos una categoría")
    private List<Long> categoriaIds;
    @NotEmpty(message = "Debe seleccionar al menos 3 jueces")
    private List<Long> juezIds;
}