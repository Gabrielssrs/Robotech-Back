package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "jueces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Juez implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false, length = 8)
    private String dni;

    @Column(nullable = false, unique = true)
    private String telefono;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contrasena;

    @ManyToOne
    @JoinColumn(name = "sede_id")
    private Sede sede;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "juez_roles",
            joinColumns = @JoinColumn(name = "juez_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_credencial", nullable = false)
    private NivelCredencial nivelCredencial;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private JuezEstado estado; // Nuevo campo para el estado

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "juez_especialidades",
            joinColumns = @JoinColumn(name = "juez_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> especialidades = new HashSet<>();

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    // --- Implementación de UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getRol().name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return this.contrasena;
    }

    @Override
    public String getUsername() {
        return this.correo;
    }

    // Para simplificar, asumimos que las cuentas de los jueces siempre están activas.
    // Podrías añadir campos booleanos como en Competidor si necesitas más control.
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() {
        // El juez puede usar el sistema si no está RETIRADO.
        return this.estado != JuezEstado.RETIRADO;
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDate.now();
        if (this.estado == null) {
            this.estado = JuezEstado.ACTIVO;
        }
    }
}