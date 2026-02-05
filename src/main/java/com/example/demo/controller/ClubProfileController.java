package com.example.demo.controller;

import com.example.demo.dto.ClubProfileResponse;
import com.example.demo.dto.ClubUpdateRequest;
import com.example.demo.model.Club;
import com.example.demo.service.ClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/club")
@RequiredArgsConstructor
public class ClubProfileController {

    private final ClubService clubService;

    @GetMapping("/perfil")
    @PreAuthorize("hasAuthority('ROLE_ADM_CLUB')")
    public ResponseEntity<ClubProfileResponse> getClubProfile() {
        // Obtenemos la autenticación del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // El 'name' de la autenticación es el correo del club que inició sesión
        String clubEmail = authentication.getName();
        // Llamamos al nuevo método del servicio que devuelve directamente el DTO
        ClubProfileResponse clubProfile = clubService.getClubProfileByEmail(clubEmail);
        return ResponseEntity.ok(clubProfile);
    }

    @PutMapping("/perfil")
    @PreAuthorize("hasAuthority('ROLE_ADM_CLUB')")
    public ResponseEntity<?> updateClubProfile(
            Authentication authentication,
            @Valid @RequestPart("details") ClubUpdateRequest clubDetails,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) {
        
        String clubEmail = authentication.getName();
        // Obtenemos el perfil actual para sacar el ID
        ClubProfileResponse currentProfile = clubService.getClubProfileByEmail(clubEmail);
        
        // Reutilizamos la lógica de actualización del servicio usando el ID obtenido
        Club clubActualizado = clubService.updateClub(currentProfile.getId(), clubDetails, logoFile);
        return ResponseEntity.ok(clubActualizado);
    }
}