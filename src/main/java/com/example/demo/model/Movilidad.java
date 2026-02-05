package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movilidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de movilidad único

    @Column(name = "velocidad_max_kmh")
    private Double velocidadMaximaKmh; // Velocidad máxima en km/h

    @Column(name = "tipo_traccion")
    private String tipoTraccion; // Ej: "Ruedas 4x4", "Orugas", "Patas Hexápodas", "Flotante"

    @Column(name = "agilidad")
    private String agilidad; // Ej: "Alta", "Media", "Baja"

    @Column(name = "terreno_ideal")
    private String terrenoIdeal; // Ej: "Urbano", "Rocoso", "Arena"

    @OneToOne
    @JoinColumn(name = "robot_id", unique = true)
    @JsonIgnore
    private Robot robot;
}