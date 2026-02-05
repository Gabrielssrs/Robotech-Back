package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JuezResponse {
    private Long id;
    private String nombre;
    private String dni;
    private String telefono;
    private String correo;
    private String nivelCredencial;
    private String estado;
    private List<String> especialidades;
    private String fotoUrl;
    private LocalDate fechaCreacion;
    private String sedeNombre;
    private Long sedeId;
    private EncuentroResponse proximoEncuentro;
}
