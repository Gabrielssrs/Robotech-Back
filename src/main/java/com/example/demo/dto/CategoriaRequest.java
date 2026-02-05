package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotBlank(message = "El tipo de competición es obligatorio")
    private String tipoCompeticion; // Cambiado de Enum a String

    @PositiveOrZero(message = "El peso debe ser un valor positivo")
    private Double pesoMaximoKg;

    private Double anchoMaximoCm;
    private Double altoMaximoCm;
    private Double largoMaximoCm;
    
    private String armaPrincipalPermitida;
    private Double velocidadMaximaPermitidaKmh;
    private String terrenoCompeticion;
    private String tipoTraccionPermitido;
    private String materialesPermitidos;
}