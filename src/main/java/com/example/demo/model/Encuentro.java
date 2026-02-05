package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "encuentros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Encuentro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "robot_a_id", nullable = false)
    private Robot robotA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "robot_b_id", nullable = false)
    private Robot robotB;

    @Column(name = "puntos_robot_a")
    private Double puntosRobotA;

    @Column(name = "puntos_robot_b")
    private Double puntosRobotB;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "robot_ganador_id") // Puede ser nulo hasta que se decida
    private Robot robotGanador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "juez_id") // El juez que califica el encuentro
    private Juez juez;

    @Column(name = "fecha_encuentro")
    private LocalDateTime fechaEncuentro;
}