package com.example.demo.controller;


import com.example.demo.dto.ClubDetailResponse;
import com.example.demo.dto.ClubUpdateRequest;
import com.example.demo.model.Club;
import com.example.demo.service.ClubService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    // Endpoint para crear un nuevo club (Create)
    @PostMapping("/registro")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> createClub(@RequestBody Club club) {
        try {
            Club nuevoClub = clubService.createClub(club);
            return new ResponseEntity<>(nuevoClub, HttpStatus.CREATED);
        } catch (Exception e) {
            // Manejo de errores, por ejemplo, si el correo o nombre ya existen (constraints de BD)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al crear el club: " + e.getMessage());
        }
    }

    // Endpoint para obtener todos los clubs (Read)
    @GetMapping
    public ResponseEntity<List<Club>> getAllClubs() {
        List<Club> clubs = clubService.getAllClubs();
        return ResponseEntity.ok(clubs);
    }

    // Endpoint para obtener un club por su ID (Read)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADM_SISTEMA', 'ROLE_ADM_CLUB')") // Aseguramos el endpoint
    public ResponseEntity<?> getClubById(@PathVariable long id) {
        // Usamos el método que devuelve el DTO para evitar LazyInitializationException
        ClubDetailResponse clubDetails = clubService.getClubDetailsById(id);
        return ResponseEntity.ok(clubDetails);
    }

    // Endpoint para obtener los detalles completos de un club, incluyendo sus competidores
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<ClubDetailResponse> getClubDetails(@PathVariable Long id) {
        ClubDetailResponse clubDetails = clubService.getClubDetailsById(id);
        return ResponseEntity.ok(clubDetails);
    }

    // Endpoint para actualizar un club (Update)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADM_SISTEMA', 'ROLE_ADM_CLUB')")
    public ResponseEntity<?> updateClub(
            @PathVariable long id,
            @Valid @RequestPart("details") ClubUpdateRequest clubDetails,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) {

        Club clubActualizado = clubService.updateClub(id, clubDetails, logoFile);
        if (clubActualizado != null) {
            return ResponseEntity.ok(clubActualizado);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club no encontrado con ID: " + id);
        }
    }

    // Endpoint para eliminar un club (Delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')") // Solo el ADMIN puede eliminar
    public ResponseEntity<?> deleteClub(@PathVariable long id) {
        clubService.deleteClub(id);
        return ResponseEntity.ok("Club con ID " + id + " eliminado correctamente.");
    }

    // Endpoint para suspender un club
    @PostMapping("/{id}/suspender")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> suspenderClub(@PathVariable long id) {
        try {
            Club clubSuspendido = clubService.suspenderClub(id);
            return ResponseEntity.ok(clubSuspendido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Endpoint para activar un club
    @PostMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<?> activarClub(@PathVariable long id) {
        try {
            Club clubActivado = clubService.activarClub(id);
            return ResponseEntity.ok(clubActivado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}