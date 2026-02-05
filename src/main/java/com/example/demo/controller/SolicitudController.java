package com.example.demo.controller;

import com.example.demo.dto.SolicitudRequest;
import jakarta.validation.Valid;
import com.example.demo.model.Solicitud;
import com.example.demo.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping
    public ResponseEntity<?> createSolicitud(@Valid @RequestBody SolicitudRequest solicitudRequest) {
        try {
            Solicitud solicitud = new Solicitud();
            solicitud.setNombreCompleto(solicitudRequest.getNombreCompleto());
            solicitud.setCorreoElectronico(solicitudRequest.getCorreoElectronico());
            solicitud.setDescripcionSolicitud(solicitudRequest.getDescripcionSolicitud());

            // Usamos Objects.requireNonNull para afirmar que el valor no es nulo y satisfacer al analizador.
            Long clubId = Objects.requireNonNull(solicitudRequest.getClubId(), "El ID del club no puede ser nulo.");

            Solicitud nuevaSolicitud = solicitudService.createSolicitud(solicitud, clubId); // Pasamos la variable local
            return new ResponseEntity<>(nuevaSolicitud, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al crear la solicitud: " + e.getMessage());
        }
    }

    @GetMapping("/mi-club")
    public ResponseEntity<List<Solicitud>> getSolicitudesPropias(Authentication authentication) {
        String clubEmail = authentication.getName();
        List<Solicitud> solicitudes = solicitudService.getSolicitudesByClub(clubEmail);
        return ResponseEntity.ok(solicitudes);
    }

    @PostMapping("/{id}/aceptar")
    public ResponseEntity<?> aceptarSolicitud(@PathVariable long id, Authentication authentication) {
        try {
            String clubEmail = authentication.getName();
            solicitudService.aceptarSolicitud(id, clubEmail);
            return ResponseEntity.ok().body("Solicitud aceptada y correo enviado.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<?> rechazarSolicitud(@PathVariable long id, Authentication authentication) {
        try {
            String clubEmail = authentication.getName();
            solicitudService.rechazarSolicitud(id, clubEmail);
            return ResponseEntity.ok().body("Solicitud rechazada y correo enviado.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}