package com.example.demo.repository;

import com.example.demo.model.ResultadoTorneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultadoTorneoRepository extends JpaRepository<ResultadoTorneo, Long> {
}