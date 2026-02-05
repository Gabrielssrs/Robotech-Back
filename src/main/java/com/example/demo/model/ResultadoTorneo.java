package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "resultados_torneo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoTorneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @ManyToOne
    @JoinColumn(name = "primer_puesto_id")
    private Robot primerPuesto;

    @ManyToOne
    @JoinColumn(name = "segundo_puesto_id")
    private Robot segundoPuesto;

    @ManyToOne
    @JoinColumn(name = "tercer_puesto_id")
    private Robot tercerPuesto;

    @Column(name = "puntos_tercer_puesto")
    private Double puntosTercerPuesto;
}
