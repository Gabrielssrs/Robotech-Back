package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "solicitudes_retiro")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudRetiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competidor_id", nullable = false)
    private Competidor competidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_gestor_id") // Puede ser nulo hasta que se gestione
    private Administrador administradorGestor;

    @OneToOne(mappedBy = "solicitudRetiro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // No usamos @JsonIgnore aquí para poder serializarlo y mostrarlo en el frontend
    private CodigoRetiro codigoRetiro;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SolicitudRetiroEstado estado;

    @Column(nullable = false)
    private LocalDate fechaSolicitud;

    @PrePersist
    protected void onCreate() {
        this.fechaSolicitud = LocalDate.now();
        if (this.estado == null) {
            this.estado = SolicitudRetiroEstado.PENDIENTE;
        }
    }
}