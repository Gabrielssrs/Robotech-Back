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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "administradores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Administrador implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false, length = 8)
    private String dni;

    @Column(nullable = true) // Lo hacemos opcional (nullable)
    private String telefono;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contrasena;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "administrador_roles", joinColumns = @JoinColumn(name = "administrador_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();

    // --- Campo para manejar el estado (Activo/Inactivo) ---
    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

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

    @Override
    public boolean isAccountNonExpired() {
        return true; // O puedes añadir un campo en la BD
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // O puedes añadir un campo en la BD
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // O puedes añadir un campo en la BD
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }
}