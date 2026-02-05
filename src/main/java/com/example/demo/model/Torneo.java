package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "torneos")
@Getter
@Setter
@AllArgsConstructor
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "fecha_inicio_inscripcion")
    private LocalDate fechaInicioInscripcion;

    @Column(name = "fecha_limite_inscripcion")
    private LocalDate fechaLimiteInscripcion;

    @ManyToOne
    @JoinColumn(name = "sede_id", nullable = false)
    private Sede sede;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TorneoEstado estado; // Campo añadido para el estado (PROXIMAMENTE, EN_CURSO, etc.)

    private boolean activo = true; // Por defecto, un torneo está activo al crearse

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "torneo_categorias",
            joinColumns = @JoinColumn(name = "torneo_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categorias = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "torneo_participantes",
            joinColumns = @JoinColumn(name = "torneo_id"),
            inverseJoinColumns = @JoinColumn(name = "robot_id")
    )
    @JsonIgnore
    private Set<Robot> participantes = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinTable(
            name = "torneo_jueces",
            joinColumns = @JoinColumn(name = "torneo_id"),
            inverseJoinColumns = @JoinColumn(name = "juez_id")
    )
    @JsonIgnore
    private Set<Juez> jueces = new HashSet<>();

    public Torneo() {
        // Constructor sin argumentos requerido por JPA
    }
}