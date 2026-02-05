package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sedes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String ciudad;

    @Column(name = "capacidad_total")
    private Integer capacidadTotal;

    @Column(name = "nro_canchas")
    private Integer nroCanchas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SedeEstado estado;

    @Column(name = "telefono_contacto")
    private String telefonoContacto;

    @PrePersist
    protected void onCreate() {
        if (this.estado == null) {
            this.estado = SedeEstado.DISPONIBLE;
        }
    }
}