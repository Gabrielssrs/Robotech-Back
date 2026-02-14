package com.example.demo.service;

import com.example.demo.model.Club;
import com.example.demo.model.ClubEstado;
import com.example.demo.repository.ClubRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuspendClubServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO suspenderClub()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | suspenderClub() | ID existente, estado ACTIVO | Club con estado SUSPENDIDO | Cambio de estado exitoso
     * 2 | suspenderClub() | ID existente, ya SUSPENDIDO | Club con estado SUSPENDIDO | Mantiene el estado (idempotente)
     * 3 | suspenderClub() | ID inexistente | RuntimeException / Null | Verifica manejo de error (asumiendo excepción)
     */

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubServiceImpl clubService;

    @Test
    void suspenderClub_ChangeToSuspended_Success() {
        // Descripción: Verifica que un club activo cambie su estado a SUSPENDIDO correctamente.
        // Arrange
        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);
        club.setNombre("Club Activo");
        club.setEstado(ClubEstado.ACTIVO);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Club result = clubService.suspenderClub(clubId);

        // Assert
        assertNotNull(result);
        assertEquals(ClubEstado.SUSPENDIDO, result.getEstado());
        verify(clubRepository).save(club);
        System.out.println("✓ Caso 1: Suspender club activo - EXITOSO");
    }

    @Test
    void suspenderClub_AlreadySuspended_NoChange() {
        // Descripción: Verifica que si el club ya está suspendido, el estado se mantenga y no ocurran errores (idempotencia).
        // Arrange
        Long clubId = 2L;
        Club club = new Club();
        club.setId(clubId);
        club.setNombre("Club Suspendido");
        club.setEstado(ClubEstado.SUSPENDIDO);

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Club result = clubService.suspenderClub(clubId);

        // Assert
        assertNotNull(result);
        assertEquals(ClubEstado.SUSPENDIDO, result.getEstado());
        verify(clubRepository).save(club);
        System.out.println("✓ Caso 2: Club ya suspendido (idempotencia) - EXITOSO");
    }

    @Test
    void suspenderClub_ClubNotFound_ThrowsException() {
        // Descripción: Verifica que se lance una excepción si se intenta suspender un club que no existe.
        // Arrange
        Long clubId = 999L;
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        // Act & Assert
        // Nota: Asumimos que el servicio lanza RuntimeException o similar cuando no encuentra el ID.
        // Si tu servicio retorna null en lugar de lanzar excepción, cambia esto a assertNull(result).
        Exception exception = assertThrows(RuntimeException.class, () -> {
            clubService.suspenderClub(clubId);
        });
        
        assertTrue(exception.getMessage() != null); // Verifica que haya algún mensaje de error
        System.out.println("✓ Caso 3: Club no encontrado - RECHAZADO");
    }
}