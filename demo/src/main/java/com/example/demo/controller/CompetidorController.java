package com.example.demo.controller;

import com.example.demo.model.Competidor;
import com.example.demo.service.CompetidorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/competidores")
@CrossOrigin(origins = "*")
public class CompetidorController {

    @Autowired
    private CompetidorService competidorService;

    @PostMapping("/registro")
    public ResponseEntity<?> registrarCompetidor(@RequestBody Competidor competidor) {
        try {
            Competidor nuevoCompetidor = competidorService.registrarCompetidor(competidor);
            return ResponseEntity.ok(Map.of("mensaje", "Registro exitoso.", "competidor", nuevoCompetidor));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}