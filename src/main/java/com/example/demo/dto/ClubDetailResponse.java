package com.example.demo.dto;

import com.example.demo.model.Club;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ClubDetailResponse {
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
    private String fotoUrl;
    private String estado; // Representación del enum ClubEstado
    private int totalCompetidores;
    private List<CompetidorSimpleDto> competidores;

    public ClubDetailResponse(Club club) {
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
        this.fotoUrl = club.getFotoUrl();
        this.estado = club.getEstado().name(); // Convertir enum a String
        this.totalCompetidores = club.getTotalCompetidores();

        // Mapear la lista de competidores a CompetidorSimpleDto
        // Aseguramos que la colección se inicialice dentro de la transacción si es LAZY
        if (club.getCompetidores() != null && !club.getCompetidores().isEmpty()) {
            this.competidores = club.getCompetidores().stream()
                    .map(CompetidorSimpleDto::new)
                    .collect(Collectors.toList());
        } else {
            this.competidores = List.of(); // Lista vacía si no hay competidores
        }
    }
}