package com.example.demo.controller;

import com.example.demo.dto.RobotRequest;
import com.example.demo.model.Robot;
import com.example.demo.service.RobotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/robots")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_COMPETIDOR', 'ROLE_ADM_SISTEMA')") // Seguridad a nivel de clase
public class RobotController {

    private final RobotService robotService;

    @PostMapping
    public ResponseEntity<Robot> createRobot(@Valid @RequestBody @NonNull RobotRequest request, @NonNull Authentication authentication) {
        Robot nuevoRobot = robotService.createRobot(request, authentication);
        return new ResponseEntity<>(nuevoRobot, HttpStatus.CREATED);
    }

    // Endpoint adicional para compatibilidad con el JS si usa /mis-robots
    @GetMapping("/mis-robots")
    public ResponseEntity<List<Robot>> getMisRobots(@NonNull Authentication authentication) {
        return getRobots(authentication);
    }

    @GetMapping
    public ResponseEntity<List<Robot>> getRobots(@NonNull Authentication authentication) {
        List<Robot> robots = robotService.getRobots(authentication);
        return ResponseEntity.ok(robots);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Robot> getRobotById(@PathVariable long id, @NonNull Authentication authentication) {
        try {
            Robot robot = robotService.getRobotById(id, authentication);
            return ResponseEntity.ok(robot);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Robot> updateRobot(@PathVariable long id, @Valid @RequestBody @NonNull RobotRequest request, @NonNull Authentication authentication) {
        try {
            Robot robotActualizado = robotService.updateRobot(id, request, authentication);
            return ResponseEntity.ok(robotActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRobot(@PathVariable long id, @NonNull Authentication authentication) {
        try {
            robotService.deleteRobot(id, authentication);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}