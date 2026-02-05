package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RobotRequest {
    @NotBlank(message = "El nombre del robot es obligatorio")
    private String nombre;
    private String descripcion;
    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    @NotNull @Valid
    private AtaqueDTO ataque;
    @NotNull @Valid
    private CaracteristicasDTO caracteristicas;
    @NotNull @Valid
    private MovilidadDTO movilidad;
}