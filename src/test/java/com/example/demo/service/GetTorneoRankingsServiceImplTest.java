package com.example.demo.service;

import com.example.demo.dto.TorneoParticipanteResponse;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTorneoRankingsServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - RANKINGS Y RESULTADOS
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | getResultadoTorneo() | TorneoId=1 (Finalizado) | ResultadoTorneo (Podio) | Retorna 1ro, 2do y 3er puesto
     * 2 | getResultadoTorneo() | TorneoId=2 (Sin resultados) | Null | Retorna null si no hay podio
     * 3 | getParticipantes() | TorneoId=1, Encuentros con puntos | Lista con puntos calculados | Verifica cálculo de puntos y estado
     * 4 | getParticipantes() | TorneoId=999 (No existe) | Excepción | IllegalArgumentException
     */

    @Mock private ResultadoTorneoRepository resultadoTorneoRepository;
    @Mock private TorneoRepository torneoRepository;
    @Mock private EncuentroRepository encuentroRepository;
    
    // Mocks adicionales para el constructor de TorneoServiceImpl
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private JuezRepository juezRepository;
    @Mock private SedeRepository sedeRepository;
    @Mock private RobotRepository robotRepository;
    @Mock private CalificacionRepository calificacionRepository;

    @InjectMocks
    private TorneoServiceImpl torneoService;

    @Test
    void getResultadoTorneo_Success() {
        // Descripción: Verifica que se obtenga el podio final de un torneo.
        Long torneoId = 1L;
        Torneo torneo = new Torneo();
        torneo.setId(torneoId);

        Robot r1 = new Robot(); r1.setNombre("Campeón");
        Robot r2 = new Robot(); r2.setNombre("Subcampeón");
        Robot r3 = new Robot(); r3.setNombre("Tercero");

        ResultadoTorneo resultado = new ResultadoTorneo();
        resultado.setTorneo(torneo);
        resultado.setPrimerPuesto(r1);
        resultado.setSegundoPuesto(r2);
        resultado.setTercerPuesto(r3);

        when(resultadoTorneoRepository.findAll()).thenReturn(List.of(resultado));

        ResultadoTorneo resp = torneoService.getResultadoTorneo(torneoId);

        assertNotNull(resp);
        assertEquals("Campeón", resp.getPrimerPuesto().getNombre());
        assertEquals("Subcampeón", resp.getSegundoPuesto().getNombre());
        assertEquals("Tercero", resp.getTercerPuesto().getNombre());
        System.out.println("✓ Caso 1: Obtener podio final - EXITOSO");
    }

    @Test
    void getResultadoTorneo_NotFound() {
        // Descripción: Verifica que retorne null si no hay podio generado para el torneo.
        Long torneoId = 2L;
        when(resultadoTorneoRepository.findAll()).thenReturn(Collections.emptyList());

        ResultadoTorneo resp = torneoService.getResultadoTorneo(torneoId);

        assertNull(resp);
        System.out.println("✓ Caso 2: Podio no encontrado (null) - EXITOSO");
    }

    @Test
    void getParticipantes_CalculatesPointsAndStatus() {
        // Descripción: Verifica el cálculo de puntos y estado de los participantes en la tabla de posiciones.
        Long torneoId = 1L;
        Torneo torneo = new Torneo();
        torneo.setId(torneoId);
        torneo.setEstado(TorneoEstado.EN_CURSO);

        Competidor comp = new Competidor();
        comp.setNombre("Juan");
        comp.setApellido("Perez");

        Robot r1 = new Robot(); r1.setId(10L); r1.setNombre("Robot A"); r1.setCompetidor(comp);
        Robot r2 = new Robot(); r2.setId(20L); r2.setNombre("Robot B"); r2.setCompetidor(comp);

        torneo.setParticipantes(new HashSet<>(Set.of(r1, r2)));

        // Encuentro donde R1 gana a R2 con puntos
        Encuentro e1 = new Encuentro();
        e1.setRobotA(r1);
        e1.setRobotB(r2);
        e1.setPuntosRobotA(10.0);
        e1.setPuntosRobotB(8.0);
        e1.setRobotGanador(r1);

        when(torneoRepository.findById(torneoId)).thenReturn(Optional.of(torneo));
        when(encuentroRepository.findByTorneoId(torneoId)).thenReturn(List.of(e1));

        List<TorneoParticipanteResponse> ranking = torneoService.getParticipantes(torneoId);

        assertNotNull(ranking);
        assertEquals(2, ranking.size());

        // Verificar Robot A (Ganador, 10 puntos)
        TorneoParticipanteResponse respA = ranking.stream().filter(r -> r.getId().equals(10L)).findFirst().orElseThrow();
        assertEquals(10.0, respA.getPuntos());
        assertEquals("Participando", respA.getEstado()); // Ganó, sigue participando

        // Verificar Robot B (Perdedor, 8 puntos)
        TorneoParticipanteResponse respB = ranking.stream().filter(r -> r.getId().equals(20L)).findFirst().orElseThrow();
        assertEquals(8.0, respB.getPuntos());
        assertEquals("Eliminado", respB.getEstado()); // Perdió en eliminación directa

        System.out.println("✓ Caso 3: Cálculo de tabla de posiciones (puntos y estado) - EXITOSO");
    }

    @Test
    void getParticipantes_TorneoNotFound_Throws() {
        // Descripción: Verifica excepción si el torneo no existe.
        when(torneoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> torneoService.getParticipantes(999L));
        System.out.println("✓ Caso 4: Torneo no encontrado para ranking - RECHAZADO");
    }
}