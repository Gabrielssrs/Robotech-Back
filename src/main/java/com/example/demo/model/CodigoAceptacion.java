package com.example.demo.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "codigos_aceptacion")
@Getter
@Setter
@NoArgsConstructor
public class CodigoAceptacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private LocalDate fechaCreacion;

    @Column(nullable = false)
    private LocalDate fechaExpiracion;

    @Column(nullable = false)
    private boolean utilizado = false; // Por defecto, un código no está utilizado

    @OneToOne
    @JoinColumn(name = "solicitud_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private Solicitud solicitud;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDate.now();
    }
}