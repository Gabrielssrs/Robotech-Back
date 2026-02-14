package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracion_sistema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionSistema {

    @Id
    @Column(name = "clave", nullable = false, unique = true)
    private String clave;

    @Column(name = "valor", nullable = false)
    private String valor;

    @Column(name = "descripcion")
    private String descripcion;
}