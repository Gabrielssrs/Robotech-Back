package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // Habilitar CORS explícitamente
                .csrf(AbstractHttpConfigurer::disable) // Deshabilitar CSRF para APIs REST
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sesiones sin estado
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Añadir filtro JWT
                .authorizeHttpRequests(auth -> auth
                        // --- Endpoints Públicos ---
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes/crear/**").permitAll() // Permitir crear solicitudes
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes").permitAll() // Permitir enviar solicitudes de inscripción
                        .requestMatchers(HttpMethod.POST, "/api/competidores/registro-con-codigo").permitAll() // CORREGIDO: La URL debe coincidir con el controlador
                        .requestMatchers(HttpMethod.GET, "/api/categorias").permitAll() // Las categorías suelen ser públicas
                        .requestMatchers(HttpMethod.GET, "/api/torneos").permitAll() // Los torneos suelen ser públicos
                        .requestMatchers(HttpMethod.GET, "/api/clubs").permitAll() // Permitir listar clubes públicamente
                        .requestMatchers(HttpMethod.GET, "/api/sedes").permitAll() // Permitir listar sedes públicamente
                        .requestMatchers(HttpMethod.GET, "/api/torneos/**").permitAll()
                        .requestMatchers("/api/ranking/**").permitAll() // <--- Agrega esta línea para hacer público el ranking
                        .requestMatchers(HttpMethod.GET, "/api/torneos/*/reglamento").permitAll() // Permitir ver reglamento

                        .requestMatchers(HttpMethod.GET, "/api/torneos", "/api/categorias").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/torneos/*/inscripcion").hasAuthority("ROLE_COMPETIDOR")


                        // --- Endpoints de Administrador de Sistema ---
                        .requestMatchers(HttpMethod.POST, "/api/jueces").hasAuthority("ROLE_ADM_SISTEMA")
                        .requestMatchers("/api/jueces/**").hasAuthority("ROLE_ADM_SISTEMA") // Incluye GET, PUT, DELETE, POST para estados
                        .requestMatchers(HttpMethod.GET, "/api/solicitudes-retiro").hasAuthority("ROLE_ADM_SISTEMA") // Ver todas las solicitudes
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes-retiro/{id}/aceptar", "/api/solicitudes-retiro/{id}/rechazar").hasAuthority("ROLE_ADM_SISTEMA") // Aceptar/Rechazar solicitudes
                        .requestMatchers("/api/clubs/**").hasAuthority("ROLE_ADM_SISTEMA")
                        .requestMatchers("/api/categorias/**").hasAuthority("ROLE_ADM_SISTEMA")
                        .requestMatchers("/api/torneos/**").hasAuthority("ROLE_ADM_SISTEMA")
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADM_SISTEMA")
                        .requestMatchers("/api/sedes/**").hasAuthority("ROLE_ADM_SISTEMA") // Gestión de sedes

                        // --- Endpoints de Administrador de Club ---
                        .requestMatchers("/api/club/profile").hasAuthority("ROLE_ADM_CLUB")
                        .requestMatchers(HttpMethod.POST, "/api/solicitudes-retiro").hasAuthority("ROLE_ADM_CLUB") // Crear solicitud de retiro
                        .requestMatchers("/api/club/solicitudes/**").hasAuthority("ROLE_ADM_CLUB")
                        .requestMatchers("/api/club/competidores/**").hasAuthority("ROLE_ADM_CLUB")

                        // --- Endpoints de Juez ---
                        .requestMatchers("/api/v1/juez/perfil").hasAuthority("ROLE_JUEZ")

                        // --- Endpoints de Competidor ---
                        .requestMatchers("/api/competidor/profile/**").hasAuthority("ROLE_COMPETIDOR")
                        .requestMatchers("/api/robots/**").hasAuthority("ROLE_COMPETIDOR")

                        // --- Cualquier otra petición debe estar autenticada ---
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}