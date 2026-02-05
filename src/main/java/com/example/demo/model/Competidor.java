package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "competidores") 
@Getter // Genera todos los getters
@Setter // Genera todos los setters
@NoArgsConstructor // Genera el constructor sin argumentos
@AllArgsConstructor // Genera el constructor con todos los argumentos
public class Competidor implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(unique = true)
    private String alias; // Campo para el alias del competidor

    @Column
    private String categoria; // Campo para la categoría (ej: "Sumo", "Seguidor de Línea")

    @Column(name = "foto_url")
    private String fotoUrl; // Campo para la URL de la foto de perfil

    @Column(unique = true, nullable = false, length = 8)
    @NonNull
    private String dni;

    @Column(nullable = false, unique = true)
    private String telefono;

    @Column(name = "email", unique = true, nullable = false)
    @NonNull
    private String correoElectronico;

    @Column(nullable = false)
    private String direccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "competidores", "robots", "roles"}) // Controla la serialización
    private Club club;

    @Column(name = "contrasena", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contrasena;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "competidor_roles",
            joinColumns = @JoinColumn(name = "competidor_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    @OneToMany(mappedBy = "competidor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Rompe el bucle de serialización Competidor -> Set<Robot>
    private Set<Robot> robots = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private CompetidorEstado estado;
    // --- Implementación de UserDetails ---

    // Campos necesarios para UserDetails que deben persistirse en la BD
    @Column(name = "is_account_non_expired", nullable = false)
    private boolean isAccountNonExpired = true;

    @Column(name = "is_account_non_locked", nullable = false)
    private boolean isAccountNonLocked = true;

    @Column(name = "is_credentials_non_expired", nullable = false)
    private boolean isCredentialsNonExpired = true;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

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
        return this.correoElectronico;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        // Un competidor puede usar el sistema si está ACTIVO o SUSPENDIDO, pero no si está RETIRADO.
        // Además verificamos el flag isEnabled general
        return (this.estado == CompetidorEstado.ACTIVO || this.estado == CompetidorEstado.SUSPENDIDO) && this.isEnabled;
    }

    @PrePersist
    protected void onCreate() {
        if (this.estado == null) {
            this.estado = CompetidorEstado.ACTIVO;
        }
        // Asegurar valores por defecto antes de persistir
        this.isAccountNonExpired = true;
        this.isAccountNonLocked = true;
        this.isCredentialsNonExpired = true;
        this.isEnabled = true;
    }
}