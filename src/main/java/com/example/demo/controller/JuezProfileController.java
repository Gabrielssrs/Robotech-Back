package com.example.demo.controller;

import com.example.demo.dto.JuezResponse;
import com.example.demo.dto.JuezProfileUpdateRequest;
import com.example.demo.service.JuezService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/juez")
@RequiredArgsConstructor
public class JuezProfileController {

    private final JuezService juezService;

    @GetMapping("/perfil")
    @PreAuthorize("hasAuthority('ROLE_JUEZ')")
    public ResponseEntity<JuezResponse> getJuezProfile(Authentication authentication) {
        String juezEmail = authentication.getName();
        JuezResponse juezProfile = juezService.getJuezByCorreo(juezEmail);
        return ResponseEntity.ok(juezProfile);
    }

    @PutMapping("/perfil")
    @PreAuthorize("hasAuthority('ROLE_JUEZ')")
    public ResponseEntity<JuezResponse> updateJuezProfile(
            Authentication authentication,
            @Valid @RequestPart("details") JuezProfileUpdateRequest updateRequest,
            @RequestPart(value = "foto", required = false) MultipartFile fotoFile) {
        String juezEmail = authentication.getName();
        JuezResponse updatedJuez = juezService.updateJuezProfile(juezEmail, updateRequest, fotoFile);
        return ResponseEntity.ok(updatedJuez);
    }
}