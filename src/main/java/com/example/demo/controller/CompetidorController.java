package com.example.demo.controller;

import com.example.demo.dto.RetiroRequest;
import com.example.demo.dto.CompetidorRegistroClubRequest;
import com.example.demo.dto.CompetidorRegistroRequest;
import jakarta.validation.Valid;
import com.example.demo.model.Competidor;
import com.example.demo.service.CompetidorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/competidores")
@RequiredArgsConstructor
public class CompetidorController {

    private final CompetidorService competidorService;

    @PostMapping("/registro")
    @PreAuthorize("hasAuthority('ROLE_ADM_CLUB')") // Asegura que solo un admin de club pueda registrar competidores
    public ResponseEntity<?> registrarCompetidorPorClub(
            @Valid @RequestBody CompetidorRegistroClubRequest request,
            Authentication authentication) {
        try {
            String clubEmail = authentication.getName();
            // Usamos el método específico para DTO que ya tienes en el servicio
            Competidor nuevoCompetidor = competidorService.registrarCompetidorPorClub(request, clubEmail);
            return new ResponseEntity<>(nuevoCompetidor, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/registro-con-codigo")
    public ResponseEntity<?> registrarConCodigo(@Valid @RequestBody @NonNull CompetidorRegistroRequest request) {
        try {
            Competidor nuevoCompetidor = competidorService.registrarConCodigo(request);
            return new ResponseEntity<>(nuevoCompetidor, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('ROLE_ADM_CLUB')")
    public ResponseEntity<?> suspenderCompetidor(@PathVariable Long id, Authentication authentication) {
        try {
            Competidor competidor = competidorService.suspenderCompetidor(id, authentication);
            return ResponseEntity.ok(competidor);
        } catch (SecurityException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('ROLE_ADM_CLUB')")
    public ResponseEntity<?> activarCompetidor(@PathVariable Long id, Authentication authentication) {
        try {
            Competidor competidor = competidorService.activarCompetidor(id, authentication);
            return ResponseEntity.ok(competidor);
        } catch (SecurityException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/retirar")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')") // Solo el admin del sistema puede ejecutar el retiro final
    public ResponseEntity<?> retirarCompetidor(@PathVariable Long id, @Valid @RequestBody RetiroRequest retiroRequest, Authentication authentication) {
        try {
            Competidor competidor = competidorService.retirarCompetidor(id, retiroRequest, authentication);
            return ResponseEntity.ok(competidor);
        } catch (SecurityException | IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}