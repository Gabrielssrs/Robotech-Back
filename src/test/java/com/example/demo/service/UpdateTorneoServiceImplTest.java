package com.example.demo.service;

import com.example.demo.dto.TorneoRequest;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTorneoServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO updateTorneo()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | updateTorneo() | Request(nombre="Torneo Actualizado", sedeId=2, estado=EN_CURSO) | Torneo actualizado | Actualiza nombre, fechas, sede, estado
     * 2 | updateTorneo() | Request(nombre="Otro Torneo Existente") [Duplicado] | Excepción | IllegalArgumentException
     * 3 | updateTorneo() | Request(sedeId=999) [Sede no existe] | Excepción | IllegalArgumentException
     * 4 | updateTorneo() | Request(categoriaIds=[100], juezIds=[200]) | Torneo actualizado | Reemplaza listas existentes
     * 5 | updateTorneo() | ID=999 [Torneo no existe] | Excepción | IllegalArgumentException
     */

    @Mock private TorneoRepository torneoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private JuezRepository juezRepository;
    @Mock private SedeRepository sedeRepository;
    
    // Dependencias del constructor
    @Mock private com.example.demo.repository.RobotRepository robotRepository;
    @Mock private com.example.demo.repository.EncuentroRepository encuentroRepository;
    @Mock private com.example.demo.repository.CalificacionRepository calificacionRepository;
    @Mock private com.example.demo.repository.ResultadoTorneoRepository resultadoTorneoRepository;

    @InjectMocks
    private TorneoServiceImpl torneoService;

    private Torneo existingTorneo;
    private Sede sedeOriginal;
    private Sede sedeNueva;

    @BeforeEach
    void setUp() {
        sedeOriginal = new Sede();
        sedeOriginal.setId(1L);
        sedeOriginal.setNombre("Sede Original");

        sedeNueva = new Sede();
        sedeNueva.setId(2L);
        sedeNueva.setNombre("Sede Nueva");

        existingTorneo = new Torneo();
        existingTorneo.setId(10L);
        existingTorneo.setNombre("Torneo Original");
        existingTorneo.setSede(sedeOriginal);
        existingTorneo.setEstado(TorneoEstado.PROXIMAMENTE);
    }

    @Test
    void updateTorneo_SuccessfulUpdateAllFields() {
        // Descripción: Verifica que se actualicen correctamente todos los campos simples (nombre, descripción, fechas, sede, estado).
        System.out.println("TEST: updateTorneo_SuccessfulUpdateAllFields - Actualizar todos los campos");

        TorneoRequest req = new TorneoRequest();
        req.setNombre("Torneo Actualizado");
        req.setDescripcion("Desc Nueva");
        req.setFechaInicio(LocalDate.now().plusDays(20));
        req.setFechaFin(LocalDate.now().plusDays(22));
        req.setHoraInicio(LocalTime.of(12, 0));
        req.setSedeId(2L);
        req.setEstado(TorneoEstado.EN_CURSO);

        when(torneoRepository.findById(10L)).thenReturn(Optional.of(existingTorneo));
        when(torneoRepository.existsByNombre("Torneo Actualizado")).thenReturn(false);
        when(sedeRepository.findById(2L)).thenReturn(Optional.of(sedeNueva));
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(i -> i.getArguments()[0]);

        Torneo result = torneoService.updateTorneo(10L, req);

        assertEquals("Torneo Actualizado", result.getNombre());
        assertEquals("Desc Nueva", result.getDescripcion());
        assertEquals(sedeNueva, result.getSede());
        assertEquals(TorneoEstado.EN_CURSO, result.getEstado());
        verify(torneoRepository).save(existingTorneo);
    }

    @Test
    void updateTorneo_DuplicateName_Throws() {
        // Descripción: Verifica que lance excepción si se intenta cambiar el nombre a uno que ya existe en otro torneo.
        System.out.println("TEST: updateTorneo_DuplicateName_Throws - Nombre duplicado lanza excepción");
        TorneoRequest req = new TorneoRequest();
        req.setNombre("Otro Torneo Existente");

        when(torneoRepository.findById(10L)).thenReturn(Optional.of(existingTorneo));
        when(torneoRepository.existsByNombre("Otro Torneo Existente")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> torneoService.updateTorneo(10L, req));
    }

    @Test
    void updateTorneo_UpdateCategoriesAndJudges() {
        // Descripción: Verifica que las listas de categorías y jueces se reemplacen correctamente con los nuevos IDs.
        System.out.println("TEST: updateTorneo_UpdateCategoriesAndJudges - Actualizar listas de categorías y jueces");
        TorneoRequest req = new TorneoRequest();
        req.setCategoriaIds(List.of(100L));
        req.setJuezIds(List.of(200L));

        Categoria cat = new Categoria(); cat.setId(100L);
        Juez juez = new Juez(); juez.setId(200L);

        when(torneoRepository.findById(10L)).thenReturn(Optional.of(existingTorneo));
        when(categoriaRepository.findAllById(List.of(100L))).thenReturn(List.of(cat));
        when(juezRepository.findAllById(List.of(200L))).thenReturn(List.of(juez));
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(i -> i.getArguments()[0]);

        Torneo result = torneoService.updateTorneo(10L, req);

        assertEquals(1, result.getCategorias().size());
        assertEquals(1, result.getJueces().size());
        assertTrue(result.getCategorias().contains(cat));
    }

    @Test
    void updateTorneo_NotFound_Throws() {
        // Descripción: Verifica que lance excepción si el ID del torneo no existe.
        System.out.println("TEST: updateTorneo_NotFound_Throws - Torneo no encontrado");
        TorneoRequest req = new TorneoRequest();
        when(torneoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> torneoService.updateTorneo(999L, req));
    }
}