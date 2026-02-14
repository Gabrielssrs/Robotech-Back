package com.example.demo.controller;

import com.example.demo.dto.ConfiguracionUpdateRequest;
import com.example.demo.model.ConfiguracionSistema;
import com.example.demo.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<List<ConfiguracionSistema>> getAllConfig() {
        return ResponseEntity.ok(configuracionService.getAll());
    }

    @PutMapping("/{clave}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> updateConfig(@PathVariable String clave, @RequestBody ConfiguracionUpdateRequest request) {
        configuracionService.updateValor(clave, request.getValor());
        return ResponseEntity.ok("Configuración actualizada correctamente.");
    }
}