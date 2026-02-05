package com.example.demo.repository;

import com.example.demo.model.SeccionReglamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeccionReglamentoRepository extends JpaRepository<SeccionReglamento, Long> {
    List<SeccionReglamento> findByTorneoIdOrderByNumOrdenAsc(Long torneoId);
}
