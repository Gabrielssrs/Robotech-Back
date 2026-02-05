package com.example.demo.repository;

import com.example.demo.model.CodigoAceptacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoAceptacionRepository extends JpaRepository<CodigoAceptacion, Long> {
    Optional<CodigoAceptacion> findByCodigo(String codigo);
}