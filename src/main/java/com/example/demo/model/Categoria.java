package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre; // Ej: "Combate - Peso Pluma", "Mini Sumo Autónomo"

    @Column(columnDefinition = "TEXT")
    private String descripcion; // Descripción de las reglas generales

    @Column(name = "tipo_competicion", nullable = false)
    private String tipoCompeticion; // Cambiado de Enum a String

    @Column(name = "peso_maximo_kg")
    private Double pesoMaximoKg; // Límite de peso en kilogramos

    @Column(name = "ancho_maximo_cm")
    private Double anchoMaximoCm; // Ancho máximo en centímetros

    @Column(name = "alto_maximo_cm")
    private Double altoMaximoCm; // Alto máximo en centímetros

    @Column(name = "largo_maximo_cm")
    private Double largoMaximoCm; // Largo máximo en centímetros

    // --- REGLAS Y RESTRICCIONES DE LA CATEGORÍA ---

    @Column(name = "arma_principal_permitida")
    private String armaPrincipalPermitida; // Ej: "Solo impacto", "Prohibido fuego"

    @Column(name = "velocidad_maxima_permitida_kmh")
    private Double velocidadMaximaPermitidaKmh; // Límite de velocidad en km/h

    @Column(name = "terreno_competicion")
    private String terrenoCompeticion; // Ej: "Arena", "Acero liso", "Urbano"

    @Column(name = "tipo_traccion_permitido")
    private String tipoTraccionPermitido; // Ej: "Solo ruedas", "Cualquiera excepto volador"

    @Column(name = "materiales_permitidos", columnDefinition = "TEXT")
    private String materialesPermitidos; // Ej: "Prohibido titanio", "Solo polímeros"

    @Column(name = "activa", nullable = false)
    private boolean activa = true; // Para habilitar o deshabilitar la categoría

    
    // 'mappedBy' apunta al campo 'categorias' en la entidad Torneo.
    @ManyToMany(mappedBy = "categorias", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Torneo> torneos = new HashSet<>();
}