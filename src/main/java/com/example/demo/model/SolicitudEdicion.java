package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "solicitud_edicion")
public class SolicitudEdicion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private Administrador administrador;

    private String token;

    private boolean aceptada;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaAceptacion;
}