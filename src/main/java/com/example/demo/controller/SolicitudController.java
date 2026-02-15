package com.example.demo.controller;

import jakarta.validation.Valid;
import com.example.demo.model.Solicitud;
import com.example.demo.dto.SolicitudRequest;
import com.example.demo.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api") // CAMBIO: Ruta base genérica para manejar /solicitudes y /club/solicitudes
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    // Endpoint PÚBLICO: Formulario de inscripción
    @PostMapping("/solicitudes")
    public ResponseEntity<?> createSolicitud(@Valid @RequestBody SolicitudRequest solicitudRequest) {
        System.out.println("DEBUG: Recibida petición POST /api/solicitudes");
        try {
            Solicitud solicitud = new Solicitud();
            solicitud.setNombreCompleto(solicitudRequest.getNombreCompleto());
            solicitud.setCorreoElectronico(solicitudRequest.getCorreoElectronico());
            solicitud.setDescripcionSolicitud(solicitudRequest.getDescripcionSolicitud());
            solicitud.setFechaSolicitud(LocalDate.now());

            Solicitud nuevaSolicitud = solicitudService.createSolicitud(solicitud, solicitudRequest.getClubId());
            System.out.println("DEBUG: Solicitud creada con ID: " + nuevaSolicitud.getId());
            return new ResponseEntity<>(nuevaSolicitud, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR createSolicitud: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            System.err.println("ERROR createSolicitud: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al crear la solicitud: " + e.getMessage());
        }
    }

    // Endpoint PRIVADO (Club): Ver solicitudes
    @GetMapping("/club/solicitudes/mi-club")
    public ResponseEntity<List<Solicitud>> getSolicitudesPropias(Authentication authentication) {
        String clubEmail = authentication.getName();
        System.out.println("DEBUG: GET /api/club/solicitudes/mi-club - Usuario: " + clubEmail);
        List<Solicitud> solicitudes = solicitudService.getSolicitudesByClub(clubEmail);
        return ResponseEntity.ok(solicitudes);
    }

    // Endpoint PRIVADO (Club): Aceptar
    @PostMapping("/club/solicitudes/{id}/aceptar")
    public ResponseEntity<?> aceptarSolicitud(@PathVariable long id, Authentication authentication) {
        System.out.println("DEBUG: POST /api/club/solicitudes/" + id + "/aceptar");
        try {
            String clubEmail = authentication.getName();
            solicitudService.aceptarSolicitud(id, clubEmail);
            return ResponseEntity.ok().body("Solicitud aceptada y correo enviado.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.err.println("ERROR aceptarSolicitud: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Endpoint PRIVADO (Club): Rechazar
    @PostMapping("/club/solicitudes/{id}/rechazar")
    public ResponseEntity<?> rechazarSolicitud(@PathVariable long id, Authentication authentication) {
        System.out.println("DEBUG: POST /api/club/solicitudes/" + id + "/rechazar");
        try {
            String clubEmail = authentication.getName();
            solicitudService.rechazarSolicitud(id, clubEmail);
            return ResponseEntity.ok().body("Solicitud rechazada y correo enviado.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.err.println("ERROR rechazarSolicitud: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}