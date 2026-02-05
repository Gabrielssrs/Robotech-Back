package com.example.demo.repository;

import com.example.demo.model.Club;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
    // Método para buscar un club por su correo electrónico
    @EntityGraph(attributePaths = {"competidores", "roles"}) // Carga las listas de competidores y roles en la misma consulta
    Optional<Club> findByCorreo(String correo);
    boolean existsByNombre(String nombre);
    boolean existsByCorreo(String correo);
    boolean existsByTelefono(String telefono);
    // Método para buscar un club por su nombre
    @EntityGraph(attributePaths = {"competidores", "roles"}) // Carga las listas de competidores y roles en la misma consulta
    Optional<Club> findByNombre(String nombre);
}// En c:/Users/ramir/OneDrive/Desktop/demo/src/main/java/com/example/demo/repository/ClubRepository.java

