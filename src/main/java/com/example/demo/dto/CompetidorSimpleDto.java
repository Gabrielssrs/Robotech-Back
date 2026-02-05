package com.example.demo.dto;

import com.example.demo.model.Competidor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompetidorSimpleDto {
    private Long id;
    private String nombreCompleto;
    private String alias;
    private String estado;
    private String fotoUrl;
    private String correoElectronico; // Campo añadido

    public CompetidorSimpleDto(Competidor competidor) {
        this.id = competidor.getId();
        this.nombreCompleto = competidor.getNombre() + " " + competidor.getApellido();
        this.alias = competidor.getAlias();
        this.estado = competidor.getEstado().name();
        this.fotoUrl = competidor.getFotoUrl();
        this.correoElectronico = competidor.getCorreoElectronico(); // Asignar el correo
    }
}