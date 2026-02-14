package com.example.demo.repository;

import com.example.demo.model.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfiguracionRepository extends JpaRepository<ConfiguracionSistema, String> {
    Optional<ConfiguracionSistema> findByClave(String clave);
}