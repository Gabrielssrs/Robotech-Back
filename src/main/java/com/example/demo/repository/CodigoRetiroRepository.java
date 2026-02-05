package com.example.demo.repository;

import com.example.demo.model.CodigoRetiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoRetiroRepository extends JpaRepository<CodigoRetiro, Long> {
    Optional<CodigoRetiro> findByCodigo(String codigo);
}