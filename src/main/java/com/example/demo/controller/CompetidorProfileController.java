package com.example.demo.controller;

import com.example.demo.dto.CompetidorUpdateRequest;
import com.example.demo.model.Competidor;
import com.example.demo.service.CompetidorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/competidor")
@RequiredArgsConstructor
public class CompetidorProfileController {

    private final CompetidorService competidorService;

    @GetMapping("/perfil")
    public ResponseEntity<Competidor> getCompetidorProfile(Authentication authentication) {
        // NOTA: Se devuelve la entidad Competidor directamente. Jackson se encargará de serializarla.
        String competidorEmail = authentication.getName();
        Competidor competidor = competidorService.getCompetidorByEmail(competidorEmail);
        return ResponseEntity.ok(competidor);
    }

    @PutMapping("/perfil")
    public ResponseEntity<Competidor> updateCompetidorProfile(
            Authentication authentication,
            @Valid @RequestPart("details") CompetidorUpdateRequest request,
            @RequestPart(value = "foto", required = false) MultipartFile fotoFile) {
        String competidorEmail = authentication.getName();
        Competidor updatedCompetidor = competidorService.updateCompetidorProfile(competidorEmail, request, fotoFile);
        return ResponseEntity.ok(updatedCompetidor);
    }
}