package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CalificacionRequest {
    @NotNull
    private Long encuentroId;
    @NotNull
    private Long robotId;
    @NotNull
    @Min(0)
    @Max(10) // Asumiendo puntaje de 0 a 10, ajusta según tus reglas
    private Double puntaje;
}
