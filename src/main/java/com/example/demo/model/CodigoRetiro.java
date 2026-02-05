package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "codigos_retiro")
@Getter
@Setter
@NoArgsConstructor
public class CodigoRetiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private boolean utilizado = false;

    @OneToOne
    @JoinColumn(name = "solicitud_retiro_id", nullable = false)
    @JsonIgnore
    private SolicitudRetiro solicitudRetiro;

    @Column(nullable = false)
    private LocalDate fechaCreacion = LocalDate.now();
}