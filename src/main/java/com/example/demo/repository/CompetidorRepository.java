package com.example.demo.repository;

import com.example.demo.model.Competidor;
import org.springframework.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompetidorRepository extends JpaRepository<Competidor, Long> {
    @NonNull Optional<Competidor> findByCorreoElectronico(@NonNull String correoElectronico);
    @NonNull Optional<Competidor> findByDni(@NonNull String dni);
    boolean existsByAlias(String alias);
    boolean existsByTelefono(String telefono);
}