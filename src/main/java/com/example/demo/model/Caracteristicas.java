package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "caracteristicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Caracteristicas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_principal")
    private String materialPrincipal; // Ej: "Acero", "Titanio", "Aluminio reforzado"

    @Column(name = "peso_kg")
    private Double pesoKg; // Peso en kilogramos

    @Column(name = "altura_cm")
    private Double alturaCm; // Altura en centímetros

    @Column(name = "ancho_cm")
    private Double anchoCm; // Ancho en centímetros

    @Column(name = "fuente_poder")
    private String fuentePoder; // Ej: "Batería de Litio", "Motor de combustión"

    @Column(name = "blindaje")   
    private String blindaje; // Descripción del tipo de blindaje o defensa

    @Column(name = "caracteristica_especial", columnDefinition = "TEXT")
    private String caracteristicaEspecial; // Un campo para describir habilidades únicas

    @OneToOne
    @JoinColumn(name = "robot_id", unique = true)
    @JsonIgnore
    private Robot robot;
}