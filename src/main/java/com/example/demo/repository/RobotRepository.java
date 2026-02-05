package com.example.demo.repository;

import com.example.demo.model.Competidor;
import com.example.demo.model.Robot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RobotRepository extends JpaRepository<Robot, Long> {
    List<Robot> findByCompetidor(Competidor competidor);
}