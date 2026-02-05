package com.example.demo.controller;

import com.example.demo.dto.SolicitudRetiroRequest;
import com.example.demo.model.SolicitudRetiro;
import com.example.demo.service.SolicitudRetiroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes-retiro")
@RequiredArgsConstructor
public class SolicitudRetiroController {

    private final SolicitudRetiroService solicitudRetiroService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADM_CLUB')")
    public ResponseEntity<SolicitudRetiro> createSolicitud(@Valid @RequestBody SolicitudRetiroRequest request, Authentication authentication) {
        SolicitudRetiro nuevaSolicitud = solicitudRetiroService.createSolicitud(request, authentication);
        return new ResponseEntity<>(nuevaSolicitud, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<List<SolicitudRetiro>> getAllSolicitudes() {
        List<SolicitudRetiro> solicitudes = solicitudRetiroService.getAllSolicitudes();
        return ResponseEntity.ok(solicitudes);
    }

    @PostMapping("/{id}/aceptar")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<SolicitudRetiro> aceptarSolicitud(@PathVariable Long id, Authentication authentication) {
        SolicitudRetiro solicitud = solicitudRetiroService.aceptarSolicitud(id, authentication);
        return ResponseEntity.ok(solicitud);
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<SolicitudRetiro> rechazarSolicitud(@PathVariable Long id, Authentication authentication) {
        SolicitudRetiro solicitud = solicitudRetiroService.rechazarSolicitud(id, authentication);
        return ResponseEntity.ok(solicitud);
    }
}