package com.example.demo.controller;

import com.example.demo.dto.SedeRequest;
import com.example.demo.model.Sede;
import com.example.demo.service.SedeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sedes")
@RequiredArgsConstructor
public class SedeController {

    private final SedeService sedeService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Sede> createSede(@Valid @RequestBody SedeRequest request) {
        Sede nuevaSede = sedeService.createSede(request);
        return new ResponseEntity<>(nuevaSede, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Sede>> getAllSedes() {
        return ResponseEntity.ok(sedeService.getAllSedes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sede> getSedeById(@PathVariable Long id) {
        return ResponseEntity.ok(sedeService.getSedeById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Sede> updateSede(@PathVariable Long id, @Valid @RequestBody SedeRequest request) {
        return ResponseEntity.ok(sedeService.updateSede(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Void> deleteSede(@PathVariable Long id) {
        sedeService.deleteSede(id);
        return ResponseEntity.noContent().build();
    }
}