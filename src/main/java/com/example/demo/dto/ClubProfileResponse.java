package com.example.demo.dto;

import com.example.demo.model.Club;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ClubProfileResponse {

    private Long id;
    private String nombre;
    private String correo;
    private String telefono;
    private String direccion;
    private String representante;
    private String descripcion;
    private String slogan;
    private LocalDate fechaCreacion;
    private String categoriasPrincipales;
    private String region;
    private String estado;
    private String fotoUrl;
    private List<CompetidorSimpleDto> competidores;

    // Constructor que mapea desde la entidad Club
    public ClubProfileResponse(Club club) {
        this.id = club.getId();
        this.nombre = club.getNombre();
        this.correo = club.getCorreo();
        this.telefono = club.getTelefono();
        this.direccion = club.getDireccion();
        this.representante = club.getRepresentante();
        this.descripcion = club.getDescripcion();
        this.slogan = club.getSlogan();
        this.fechaCreacion = club.getFechaCreacion();
        this.categoriasPrincipales = club.getCategoriasPrincipales();
        this.region = club.getRegion();
        this.estado = club.getEstadoString();
        this.fotoUrl = club.getFotoUrl();
        // Mapeamos la lista de competidores a un DTO más simple para evitar bucles
        this.competidores = club.getCompetidores().stream()
                .map(CompetidorSimpleDto::new)
                .collect(Collectors.toList());
    }
}