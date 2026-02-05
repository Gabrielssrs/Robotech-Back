package com.example.demo.controller;

import com.example.demo.dto.AdminResponse;
import com.example.demo.dto.AdminUpdateRequest;
import com.example.demo.service.AdministradorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AdministradorService administradorService;

    @GetMapping("/perfil")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<AdminResponse> getAdminProfile(Authentication authentication) {
        String adminEmail = authentication.getName();
        AdminResponse adminProfile = administradorService.getAdminByEmail(adminEmail);
        return ResponseEntity.ok(adminProfile);
    }

    @PutMapping("/perfil")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<AdminResponse> updateAdminProfile(
            Authentication authentication,
            @Valid @RequestBody AdminUpdateRequest updateRequest) {
        String adminEmail = authentication.getName();
        // Pasamos null para el archivo de foto, ya que no se usa.
        AdminResponse updatedAdmin = administradorService.updateAdmin(adminEmail, updateRequest, null);
        return ResponseEntity.ok(updatedAdmin);
    }
}