package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "secciones_reglamento")
@Data
public class SeccionReglamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @Column(name = "num_orden")
    private Integer numOrden;

    @Column(name = "titulo_menu")
    private String tituloMenu;

    private String icono;

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Regla> reglas = new ArrayList<>();
}
