package com.example.demo.controller;

import com.example.demo.dto.SeccionReglamentoDto;
import com.example.demo.service.ReglamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/torneos")
@RequiredArgsConstructor
public class ReglamentoController {

    private final ReglamentoService reglamentoService;

    @GetMapping("/{id}/reglamento")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SeccionReglamentoDto>> getReglamento(@PathVariable Long id) {
        return ResponseEntity.ok(reglamentoService.getReglamentoByTorneo(id));
    }

    @PostMapping("/{id}/secciones")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<SeccionReglamentoDto> createSeccion(@PathVariable Long id, @RequestBody SeccionReglamentoDto dto) {
        return ResponseEntity.ok(reglamentoService.createSeccion(id, dto));
    }

    @PutMapping("/secciones/{seccionId}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<SeccionReglamentoDto> updateSeccion(@PathVariable Long seccionId, @RequestBody SeccionReglamentoDto dto) {
        return ResponseEntity.ok(reglamentoService.updateSeccion(seccionId, dto));
    }

    @DeleteMapping("/secciones/{seccionId}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Void> deleteSeccion(@PathVariable Long seccionId) {
        reglamentoService.deleteSeccion(seccionId);
        return ResponseEntity.ok().build();
    }
}


/* y este js pues quiero argegar lo que seria una fecha de incripciones validas que pueden ser de 3 dias , 7 o 15 dias a la hora de crearlo debo poder selccionarlo  */