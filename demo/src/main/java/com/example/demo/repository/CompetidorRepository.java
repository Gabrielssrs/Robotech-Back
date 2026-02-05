package com.example.demo.repository;

import com.example.demo.model.Competidor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompetidorRepository extends JpaRepository<Competidor, Long> {

    Optional<Competidor> findByEmail(String email);

    Optional<Competidor> findByDni(String dni);
}