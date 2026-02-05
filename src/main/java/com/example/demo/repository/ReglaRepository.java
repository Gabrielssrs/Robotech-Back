package com.example.demo.repository;

import com.example.demo.model.Regla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReglaRepository extends JpaRepository<Regla, Long> {
}
