package com.example.demo.repository;

import com.example.demo.model.Encuentro;
import com.example.demo.model.Juez;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EncuentroRepository extends JpaRepository<Encuentro, Long> {
    List<Encuentro> findByTorneoId(Long torneoId);
    List<Encuentro> findByJuezAndFechaEncuentroAfterOrderByFechaEncuentroAsc(Juez juez, LocalDateTime fecha);
    List<Encuentro> findByFechaEncuentroBetween(LocalDateTime inicio, LocalDateTime fin);
    Encuentro findTopByOrderByFechaEncuentroDesc();
}