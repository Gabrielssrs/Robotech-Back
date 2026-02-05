package com.example.demo.repository;

import com.example.demo.model.Juez;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JuezRepository extends JpaRepository<Juez, Long> {
    Optional<Juez> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    boolean existsByDni(String dni);
    boolean existsByTelefono(String telefono);
}