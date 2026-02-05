package com.example.demo.repository;

import com.example.demo.model.Calificacion;
import com.example.demo.model.Encuentro;
import com.example.demo.model.Robot;
import com.example.demo.model.Juez;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {
    List<Calificacion> findByEncuentroAndRobot(Encuentro encuentro, Robot robot);
    Optional<Calificacion> findByEncuentroAndRobotAndJuez(Encuentro encuentro, Robot robot, Juez juez);
}
