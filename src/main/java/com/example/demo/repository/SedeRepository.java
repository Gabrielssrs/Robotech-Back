package com.example.demo.repository;

import com.example.demo.model.Sede;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SedeRepository extends JpaRepository<Sede, Long> {
    boolean existsByNombre(String nombre);
}