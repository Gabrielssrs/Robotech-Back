package com.example.demo.service;

import com.example.demo.dto.TorneoRequest;
import com.example.demo.model.*;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.JuezRepository;
import com.example.demo.repository.SedeRepository;
import com.example.demo.repository.TorneoRepository;
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
class CreateTorneoServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO createTorneo()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | createTorneo() | Request válido (fechas manuales) | Torneo creado | Estado PROXIMAMENTE por defecto
     * 2 | createTorneo() | Request válido (fechas auto) | Torneo creado | Calcula fechas inicio/fin
     * 3 | createTorneo() | Nombre duplicado | Excepción | RuntimeException (wrap IllegalArgument)
     * 4 | createTorneo() | Sede inexistente | Excepción | RuntimeException (wrap IllegalArgument)
     * 5 | createTorneo() | Con Categorías y Jueces | Torneo creado | Listas pobladas correctamente
     */

    @Mock
    private TorneoRepository torneoRepository;
    @Mock
    private CategoriaRepository categoriaRepository;
    @Mock
    private JuezRepository juezRepository;
    @Mock
    private SedeRepository sedeRepository;

    // Mocks adicionales requeridos por el constructor de TorneoServiceImpl
    @Mock private com.example.demo.repository.RobotRepository robotRepository;
    @Mock private com.example.demo.repository.EncuentroRepository encuentroRepository;
    @Mock private com.example.demo.repository.CalificacionRepository calificacionRepository;
    @Mock private com.example.demo.repository.ResultadoTorneoRepository resultadoTorneoRepository;

    @InjectMocks
    private TorneoServiceImpl torneoService;

    private TorneoRequest request;
    private Sede sede;

    @BeforeEach
    void setUp() {
        sede = new Sede();
        sede.setId(1L);
        sede.setNombre("Sede Central");

        request = new TorneoRequest();
        request.setNombre("Torneo RoboWar 2024");
        request.setSedeId(1L);
        request.setHoraInicio(LocalTime.of(10, 0));
        // Configuración por defecto: fechas manuales
        request.setFechaInicio(LocalDate.now().plusDays(10));
        request.setFechaFin(LocalDate.now().plusDays(12));
        request.setFechaLimiteInscripcion(LocalDate.now().plusDays(5));
    }

    @Test
    void createTorneo_ManualDates_Success() {
        // Descripción: Verifica la creación exitosa de un torneo proporcionando fechas de inicio y fin manualmente.
        System.out.println("TEST: createTorneo_ManualDates_Success - Creación con fechas manuales");

        when(torneoRepository.existsByNombre(request.getNombre())).thenReturn(false);
        when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(i -> i.getArguments()[0]);

        Torneo result = torneoService.createTorneo(request);

        assertNotNull(result);
        assertEquals("Torneo RoboWar 2024", result.getNombre());
        assertEquals(TorneoEstado.PROXIMAMENTE, result.getEstado());
        assertEquals(sede, result.getSede());
        verify(torneoRepository).save(any(Torneo.class));
    }

    @Test
    void createTorneo_AutoDates_Success() {
        // Descripción: Verifica la creación exitosa usando la lógica de fechas automáticas (inicio inscripción + duración).
        System.out.println("TEST: createTorneo_AutoDates_Success - Creación con cálculo automático de fechas");

        request.setFechaInicio(null);
        request.setFechaFin(null);
        request.setFechaLimiteInscripcion(null);
        
        LocalDate startInsc = LocalDate.now();
        request.setFechaInicioInscripcion(startInsc);
        request.setDiasInscripcion(5);

        when(torneoRepository.existsByNombre(request.getNombre())).thenReturn(false);
        when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(i -> i.getArguments()[0]);

        Torneo result = torneoService.createTorneo(request);

        assertNotNull(result);
        // Según lógica del servicio: finInscripcion = start + days. inicioTorneo = finInscripcion.
        assertEquals(startInsc, result.getFechaInicioInscripcion());
        assertEquals(startInsc.plusDays(5), result.getFechaLimiteInscripcion());
        assertEquals(startInsc.plusDays(5), result.getFechaInicio());
    }

    @Test
    void createTorneo_DuplicateName_Throws() {
        // Descripción: Verifica que se lance una excepción si el nombre del torneo ya existe.
        System.out.println("TEST: createTorneo_DuplicateName_Throws - Nombre duplicado");

        when(torneoRepository.existsByNombre(request.getNombre())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> torneoService.createTorneo(request));
        assertTrue(ex.getMessage().contains("nombre del torneo ya existe"));
    }

    @Test
    void createTorneo_SedeNotFound_Throws() {
        // Descripción: Verifica que se lance una excepción si el ID de la sede no existe.
        System.out.println("TEST: createTorneo_SedeNotFound_Throws - Sede no encontrada");

        when(torneoRepository.existsByNombre(request.getNombre())).thenReturn(false);
        when(sedeRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> torneoService.createTorneo(request));
        assertTrue(ex.getMessage().contains("Sede no encontrada"));
    }

    @Test
    void createTorneo_WithCategoriesAndJudges_Success() {
        // Descripción: Verifica que se asignen correctamente las categorías y jueces al crear el torneo.
        System.out.println("TEST: createTorneo_WithCategoriesAndJudges_Success - Asignación de categorías y jueces");

        request.setCategoriaIds(List.of(10L, 20L));
        request.setJuezIds(List.of(5L));

        Categoria c1 = new Categoria(); c1.setId(10L);
        Categoria c2 = new Categoria(); c2.setId(20L);
        Juez j1 = new Juez(); j1.setId(5L);

        when(torneoRepository.existsByNombre(request.getNombre())).thenReturn(false);
        when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
        when(categoriaRepository.findAllById(request.getCategoriaIds())).thenReturn(List.of(c1, c2));
        when(juezRepository.findAllById(request.getJuezIds())).thenReturn(List.of(j1));
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(i -> i.getArguments()[0]);

        Torneo result = torneoService.createTorneo(request);

        assertNotNull(result);
        assertEquals(2, result.getCategorias().size());
        assertEquals(1, result.getJueces().size());
    }

    @Test
    void createTorneo_InvalidDates_Throws() {
        // Descripción: Verifica que se lance una excepción si la fecha de fin es anterior a la fecha de inicio.
        System.out.println("TEST: createTorneo_InvalidDates_Throws - Fechas inválidas (Fin antes de Inicio)");

        request.setFechaInicio(LocalDate.now().plusDays(10));
        request.setFechaFin(LocalDate.now().plusDays(5)); // Error: Fin antes que inicio

        // Mock para evitar fallo en validación de nombre antes de llegar a fechas
        when(torneoRepository.existsByNombre(request.getNombre())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> torneoService.createTorneo(request));
    }

    @Test
    void createTorneo_InvalidCategoriaId_Ignores() {
        // Descripción: Verifica que si se proporciona un ID de categoría que no existe, el servicio lo ignora y crea el torneo sin ella.
        System.out.println("TEST: createTorneo_InvalidCategoriaId_Ignores - Categoría inexistente se ignora");

        request.setCategoriaIds(List.of(999L)); // ID que no existe

        when(torneoRepository.existsByNombre(request.getNombre())).thenReturn(false);
        when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
        when(categoriaRepository.findAllById(request.getCategoriaIds())).thenReturn(List.of()); // Retorna lista vacía
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(i -> i.getArguments()[0]);

        Torneo result = torneoService.createTorneo(request);
        assertNotNull(result);
        assertTrue(result.getCategorias() == null || result.getCategorias().isEmpty());
    }
}