package com.example.demo.config;

import com.example.demo.model.Administrador;
import com.example.demo.model.Rol;
import com.example.demo.model.RolNombre;
import com.example.demo.repository.AdministradorRepository;
import com.example.demo.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdministradorRepository administradorRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        // Crear roles si no existen
        for (RolNombre rolNombre : RolNombre.values()) {
            if (rolRepository.findByRol(rolNombre).isEmpty()) {
                rolRepository.save(new Rol(null, rolNombre, true));
            }
        }

        // Crear el administrador del sistema si no existe
        if (administradorRepository.findByCorreo(adminEmail).isEmpty()) {
            Rol adminRol = rolRepository.findByRol(RolNombre.ROLE_ADM_SISTEMA)
                    .orElseThrow(() -> new RuntimeException("Rol de administrador no encontrado"));

            Administrador admin = new Administrador();
            admin.setCorreo(adminEmail);
            admin.setContrasena(passwordEncoder.encode(adminPassword));
            admin.setRoles(Set.of(adminRol));
            // Dejamos los datos personales para que el admin los complete
            admin.setNombre("Administrador del Sistema"); // Nombre por defecto
            admin.setDni("00000000"); // DNI por defecto para cumplir restricción NOT NULL
            admin.setTelefono(null); // Teléfono nulo inicialmente
            admin.setEnabled(true); // Hacemos explícito que el admin se crea activo
            administradorRepository.save(admin);
        }
    }
}