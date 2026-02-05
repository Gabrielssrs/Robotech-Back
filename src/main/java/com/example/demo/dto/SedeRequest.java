package com.example.demo.dto;

import com.example.demo.model.SedeEstado;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SedeRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;
    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;
    @Min(value = 0, message = "La capacidad debe ser mayor o igual a 0")
    private Integer capacidadTotal;
    @Min(value = 0, message = "El número de canchas debe ser mayor o igual a 0")
    private Integer nroCanchas;
    private SedeEstado estado;
    private String telefonoContacto;
}