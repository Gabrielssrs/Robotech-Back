package com.example.demo.repository;

import com.example.demo.model.SolicitudRetiro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudRetiroRepository extends JpaRepository<SolicitudRetiro, Long> {
    // Métodos de consulta personalizados si son necesarios en el futuro.
}