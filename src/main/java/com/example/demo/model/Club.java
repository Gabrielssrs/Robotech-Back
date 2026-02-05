package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.hibernate.annotations.Formula;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "clubs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Club implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "telefono", unique = true)
    private String telefono;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "representante")
    private String representante;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "slogan")
    private String slogan;

    @Column(name = "contrasena", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contrasena;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "categorias_principales")
    private String categoriasPrincipales;

    @Column(name = "region")
    private String region;

    // --- Campos para UserDetails ---
    @Column(name = "is_account_non_expired", nullable = false)
    private boolean isAccountNonExpired = true;
    @Column(name = "is_account_non_locked", nullable = false)
    private boolean isAccountNonLocked = true;
    @Column(name = "is_credentials_non_expired", nullable = false)
    private boolean isCredentialsNonExpired = true;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "club_roles",
            joinColumns = @JoinColumn(name = "club_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Evita la serialización en bucle al devolver la entidad Club directamente.
    private Set<Competidor> competidores = new HashSet<>();

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Evita la serialización en bucle al devolver la entidad Club directamente.
    private Set<Robot> robots = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDate.now();
        }
        // Asignar estado por defecto al crear un club
        if (estado == null) {
            estado = ClubEstado.ACTIVO;
        }
    }
    @Lob
    @Column(name = "foto_url")
    private String fotoUrl;


    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private ClubEstado estado;
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
        // Lógica de negocio: El club tiene acceso si NO está retirado.
        // Esto permite el acceso cuando está ACTIVO o SUSPENDIDO.
        return this.estado != ClubEstado.RETIRADO;
    }

    /**
     * Devuelve el estado del club como una cadena de texto.
     * Jackson usará este método para serializar el campo 'estado' en el JSON.
     */
    @JsonProperty("estado") // Asegura que el JSON tenga un campo "estado" con este valor
    public String getEstadoString() {
        return this.estado.name();
    }

    /**
     * Devuelve el número total de competidores asociados a este club, calculado directamente
     * por Hibernate para evitar problemas de LazyInitializationException.
     */
    @Formula("(select count(*) from competidores c where c.club_id = id)")
    private int totalCompetidores;
}