package com.example.demo.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "correo_electronico", nullable = false)
    private String correoElectronico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(name = "descripcion_solicitud", columnDefinition = "TEXT")
    private String descripcionSolicitud;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private SolicitudEstado estado;

    // Relación 1 a 1 con CodigoAceptacion. CascadeType.ALL asegura que si guardamos/borramos una solicitud, su código asociado también lo haga.
    @OneToOne(mappedBy = "solicitud", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private CodigoAceptacion codigoAceptacion;

    @PrePersist
    protected void onCreate() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDate.now();
        }
        if (estado == null) {
            estado = SolicitudEstado.PENDIENTE;
        }
    }
}