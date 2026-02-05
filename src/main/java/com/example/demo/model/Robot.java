package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "robots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Robot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RobotEstado estado;

    @Column(name = "reparacion_hasta")
    private LocalDate reparacionHasta; // Fecha hasta la que el robot está en reparación

    @Column(name = "puntos", nullable = false)
    private Integer puntos = 0; // Puntos acumulados para el ranking global

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competidor_id", nullable = false)
    @JsonIgnore // Rompe el bucle de serialización Robot -> Competidor
    private Competidor competidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    @JsonIgnore // Rompe el bucle de serialización Robot -> Club
    private Club club;

    @OneToOne(mappedBy = "robot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Rompe el bucle de serialización Robot -> Ataque
    private Ataque ataque;

    @OneToOne(mappedBy = "robot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Rompe el bucle de serialización Robot -> Caracteristicas
    private Caracteristicas caracteristicas;

    @OneToOne(mappedBy = "robot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Rompe el bucle de serialización Robot -> Movilidad
    private Movilidad movilidad;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToMany(mappedBy = "participantes")
    @JsonIgnore // Rompe el bucle de serialización Robot -> Set<Torneo>
    private Set<Torneo> torneos = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDate.now();
        }
        if (estado == null) {
            estado = RobotEstado.ACTIVO;
        }
        if (puntos == null) {
            puntos = 0;
        }
    }
}