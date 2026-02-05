package com.example.demo.repository;

import com.example.demo.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    boolean existsByNombre(String nombre);
    List<Torneo> findByFechaInicio(LocalDate fechaInicio);
    List<Torneo> findByJueces_Correo(String correo);
    List<Torneo> findByParticipantes_Competidor_CorreoElectronico(String correo);
}