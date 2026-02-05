package com.example.demo.controller;

import com.example.demo.dto.JuezRequest;
import com.example.demo.dto.JuezResponse;
import com.example.demo.dto.JuezUpdateRequest;
import com.example.demo.service.JuezService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jueces")
@RequiredArgsConstructor
public class JuezController {

    private final JuezService juezService;

    // Protegido: Solo el administrador del sistema puede crear jueces
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<JuezResponse> createJuez(@Valid @RequestBody JuezRequest juezRequest) {
        JuezResponse nuevoJuez = juezService.createJuez(juezRequest);
        return new ResponseEntity<>(nuevoJuez, HttpStatus.CREATED);
    }

    // Público o protegido, según necesidad. Aquí lo dejamos para administradores.
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<List<JuezResponse>> getAllJueces() {
        return ResponseEntity.ok(juezService.getAllJueces());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<JuezResponse> getJuezById(@PathVariable long id) {
        return ResponseEntity.ok(juezService.getJuezById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<JuezResponse> updateJuez(@PathVariable long id, @Valid @RequestBody JuezUpdateRequest request) {
        JuezResponse juezActualizado = juezService.updateJuez(id, request);
        return ResponseEntity.ok(juezActualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Void> deleteJuez(@PathVariable long id) {
        juezService.deleteJuez(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints para cambiar estado ---

    @PostMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Void> suspenderJuez(@PathVariable long id) {
        juezService.suspenderJuez(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Void> activarJuez(@PathVariable long id) {
        juezService.activarJuez(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/retirar")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Void> retirarJuez(@PathVariable long id) {
        juezService.retirarJuez(id);
        return ResponseEntity.ok().build();
    }
}