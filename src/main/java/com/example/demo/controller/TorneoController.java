package com.example.demo.controller;

import com.example.demo.dto.CalificacionRequest;
import com.example.demo.dto.TorneoParticipanteResponse;
import com.example.demo.dto.TorneoRequest;
import com.example.demo.dto.TorneoResponse;
import com.example.demo.dto.EncuentroResponse;
import com.example.demo.model.Torneo;
import com.example.demo.model.ResultadoTorneo;
import com.example.demo.service.TorneoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/torneos")
@RequiredArgsConstructor
public class TorneoController {

    private final TorneoService torneoService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> createTorneo(@RequestBody TorneoRequest request) {
        try {
            Torneo nuevoTorneo = torneoService.createTorneo(request);
            return new ResponseEntity<>(nuevoTorneo, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al crear el torneo: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> updateTorneo(@PathVariable Long id, @RequestBody TorneoRequest request) {
        try {
            Torneo torneoActualizado = torneoService.updateTorneo(id, request);
            return ResponseEntity.ok(torneoActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<TorneoResponse>> getAllTorneos() {
        return ResponseEntity.ok(torneoService.getAllTorneos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TorneoResponse> getTorneoById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(torneoService.getTorneoById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{id}/inscripcion")
    @PreAuthorize("hasAuthority('ROLE_COMPETIDOR')")
    public ResponseEntity<?> inscribirRobot(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        try {
            Long robotId = payload.get("robotId");
            torneoService.inscribirRobot(id, robotId);
            return ResponseEntity.ok().body(Map.of("message", "Inscripción exitosa"));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/participantes")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<TorneoParticipanteResponse>> getParticipantes(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.getParticipantes(id));
    }

    @GetMapping("/{id}/encuentros")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<EncuentroResponse>> getEncuentros(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.getEncuentros(id));
    }

    @GetMapping("/encuentros/{id}")
    @PreAuthorize("hasAuthority('ROLE_JUEZ') or hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<EncuentroResponse> getEncuentroById(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.getEncuentroById(id));
    }

    @PostMapping("/{id}/simular-inscripcion")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> simularInscripcion(@PathVariable Long id) {
        torneoService.simularInscripcionMasiva(id);
        return ResponseEntity.ok().body(Map.of("message", "Inscripción masiva completada"));
    }

    @PostMapping("/encuentros/{id}/resultado")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> registrarResultado(@PathVariable Long id, @RequestBody Map<String, Long> payload) {
        torneoService.registrarGanador(id, payload.get("ganadorId"));
        return ResponseEntity.ok().body(Map.of("message", "Resultado registrado"));
    }

    @PostMapping("/encuentros/{id}/simular")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> simularEncuentro(@PathVariable Long id) {
        torneoService.simularEncuentro(id);
        return ResponseEntity.ok().body(Map.of("message", "Encuentro simulado con puntajes"));
    }

    @GetMapping("/mis-torneos")
    @PreAuthorize("hasAuthority('ROLE_JUEZ')")
    public ResponseEntity<List<TorneoResponse>> getMisTorneos(Authentication authentication) {
        return ResponseEntity.ok(torneoService.getTorneosAsignados(authentication.getName()));
    }

    @GetMapping("/mis-inscripciones")
    @PreAuthorize("hasAuthority('ROLE_COMPETIDOR')")
    public ResponseEntity<List<TorneoResponse>> getMisInscripciones(Authentication authentication) {
        return ResponseEntity.ok(torneoService.getTorneosInscritos(authentication.getName()));
    }

    @PostMapping("/calificar")
    @PreAuthorize("hasAuthority('ROLE_JUEZ')")
    public ResponseEntity<?> calificarRobot(@Valid @RequestBody CalificacionRequest request, Authentication authentication) {
        torneoService.calificarRobot(authentication.getName(), request);
        return ResponseEntity.ok().body(Map.of("message", "Calificación registrada correctamente"));
    }

    @GetMapping("/{id}/resultado")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ResultadoTorneo> getResultadoTorneo(@PathVariable Long id) {
        return ResponseEntity.ok(torneoService.getResultadoTorneo(id));
    }
}