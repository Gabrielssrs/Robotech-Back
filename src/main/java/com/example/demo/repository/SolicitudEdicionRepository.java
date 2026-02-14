package com.example.demo.repository;

import com.example.demo.model.SolicitudEdicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface SolicitudEdicionRepository extends JpaRepository<SolicitudEdicion, Long> {
    Optional<SolicitudEdicion> findByToken(String token);

    // Verifica si hay una solicitud aceptada para este admin en el rango de tiempo dado (ej. últimos 15 min)
    @Query("SELECT COUNT(s) > 0 FROM SolicitudEdicion s " +
           "WHERE s.administrador.id = :adminId AND s.aceptada = true " +
           "AND s.fechaAceptacion > :tiempoLimite")
    boolean isEdicionHabilitada(@Param("adminId") Long adminId, @Param("tiempoLimite") LocalDateTime tiempoLimite);
}