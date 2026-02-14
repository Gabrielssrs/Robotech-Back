package com.example.demo.service;

import com.example.demo.model.Club;
import com.example.demo.repository.ClubRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListClubServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO getAllClubs()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | getAllClubs() | DB=[Club1, Club2] | Lista de Clubes (size=2) | Retorna todos los clubes existentes
     * 2 | getAllClubs() | DB=[] | Lista vacía | Retorna lista vacía sin errores
     */

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubServiceImpl clubService;

    @Test
    void getAllClubs_WithData_ReturnsList() {
        // Descripción: Verifica que el servicio retorne una lista con los clubes encontrados cuando existen registros en la base de datos.
        // Arrange
        Club club1 = new Club();
        club1.setId(1L);
        club1.setNombre("Club Alpha");

        Club club2 = new Club();
        club2.setId(2L);
        club2.setNombre("Club Beta");

        when(clubRepository.findAll()).thenReturn(Arrays.asList(club1, club2));

        // Act
        List<Club> result = clubService.getAllClubs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Club Alpha", result.get(0).getNombre());
        verify(clubRepository, times(1)).findAll();
        System.out.println("✓ Caso 1: Listado con datos existentes - EXITOSO");
    }

    @Test
    void getAllClubs_NoData_ReturnsEmptyList() {
        // Descripción: Verifica que el servicio retorne una lista vacía (y no null) cuando no existen registros en la base de datos.
        // Arrange
        when(clubRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Club> result = clubService.getAllClubs();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(clubRepository, times(1)).findAll();
        System.out.println("✓ Caso 2: Listado vacío (sin datos) - EXITOSO");
    }
}