package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "calificaciones")
@Data
public class Calificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "encuentro_id", nullable = false)
    private Encuentro encuentro;

    @ManyToOne
    @JoinColumn(name = "juez_id", nullable = false)
    private Juez juez;

    @ManyToOne
    @JoinColumn(name = "robot_id", nullable = false)
    private Robot robot;

    @Column(nullable = false)
    private Double puntaje;
}
