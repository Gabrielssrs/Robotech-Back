package com.example.demo.controller;

import com.example.demo.dto.AdminCreateRequest;
import com.example.demo.dto.AdminResponse;
import com.example.demo.dto.AdminUpdateRequest;
import com.example.demo.service.SystemAdminService;
import com.example.demo.service.ConfiguracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admins")
@RequiredArgsConstructor
public class SystemAdminController {

    private final SystemAdminService systemAdminService;
    private final ConfiguracionService configuracionService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody AdminCreateRequest request) {
        try {
            AdminResponse newAdmin = systemAdminService.createAdmin(request);
            return new ResponseEntity<>(newAdmin, HttpStatus.CREATED);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Captura errores inesperados (como el de SQL) para no devolver 403 mudo
            System.err.println("Error interno al crear admin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        return ResponseEntity.ok(systemAdminService.getAllAdmins());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> updateAdmin(@PathVariable Long id, @Valid @RequestBody AdminUpdateRequest request) {
        try {
            AdminResponse updatedAdmin = systemAdminService.updateAdmin(id, request);
            return ResponseEntity.ok(updatedAdmin);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Endpoint para solicitar el desbloqueo del Admin ID 1
    @PostMapping("/{id}/solicitar-desbloqueo")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> solicitarDesbloqueo(@PathVariable Long id) {
        systemAdminService.solicitarDesbloqueoEdicion(id);
        return ResponseEntity.ok("Solicitud enviada al correo principal. Revise su bandeja de entrada.");
    }

    // Endpoint público (o protegido) que recibe el click del correo
    // Nota: Se usa GET porque es un link en un email
    @GetMapping("/desbloquear")
    public ResponseEntity<?> desbloquearEdicion(@RequestParam("token") String token) {
        systemAdminService.aceptarDesbloqueoEdicion(token);
        // Redirigir al frontend (ajusta la URL si tu puerto o dominio es diferente)
        
        // Obtener la URL base configurada (Local o GitHub Pages según el entorno)
        String baseUrl = configuracionService.getValor("BASE_URL", "http://127.0.0.1:5501");
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", baseUrl + "/admin.html?desbloqueo=exitoso")
                .build();
    }

    @PostMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> suspenderAdmin(@PathVariable Long id) {
        systemAdminService.suspenderAdmin(id);
        return ResponseEntity.ok("Administrador suspendido correctamente.");
    }

    @PostMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> activarAdmin(@PathVariable Long id) {
        systemAdminService.activarAdmin(id);
        return ResponseEntity.ok("Administrador activado correctamente.");
    }
}