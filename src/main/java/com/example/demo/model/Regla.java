package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "reglas")
@Data
public class Regla{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seccion_id", nullable = false)
    private SeccionReglamento seccion;

    private String subtitulo;

    @Column(name = "texto_cuerpo", columnDefinition = "TEXT")
    private String textoCuerpo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_bloque")
    private TipoBloque tipoBloque;

    @Column(name = "num_suborden")
    private Double numSuborden;
}
