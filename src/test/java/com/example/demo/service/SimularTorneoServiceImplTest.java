package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimularTorneoServiceImplTest {

    @Mock private TorneoRepository torneoRepository;
    @Mock private EncuentroRepository encuentroRepository;
    @Mock private CalificacionRepository calificacionRepository;
    @Mock private RobotRepository robotRepository;
    @Mock private JuezRepository juezRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private SedeRepository sedeRepository;
    @Mock private ResultadoTorneoRepository resultadoTorneoRepository;

    @InjectMocks
    private TorneoServiceImpl torneoService;

    private Torneo torneo;
    private Encuentro encuentro;
    private Robot robotA;
    private Robot robotB;
    private Juez juez1, juez2, juez3;

    @BeforeEach
    void setUp() {
        juez1 = new Juez(); juez1.setId(1L);
        juez2 = new Juez(); juez2.setId(2L);
        juez3 = new Juez(); juez3.setId(3L);

        torneo = new Torneo();
        torneo.setId(1L);
        torneo.setJueces(new HashSet<>(Set.of(juez1, juez2, juez3)));
        torneo.setEstado(TorneoEstado.EN_CURSO);

        robotA = new Robot(); robotA.setId(10L); robotA.setNombre("Robot A");
        robotB = new Robot(); robotB.setId(20L); robotB.setNombre("Robot B");

        encuentro = new Encuentro();
        encuentro.setId(100L);
        encuentro.setTorneo(torneo);
        encuentro.setRobotA(robotA);
        encuentro.setRobotB(robotB);
    }

    @Test
    void simularEncuentro_Success() {
        // Descripción: Verifica que al simular un encuentro se generen calificaciones para los 3 jueces,
        // se calcule el promedio y se guarde el ganador en el encuentro.
        
        when(encuentroRepository.findById(100L)).thenReturn(Optional.of(encuentro));
        // Simulamos que no existen calificaciones previas para que entre al bloque de guardar
        when(calificacionRepository.findByEncuentroAndRobotAndJuez(any(), any(), any())).thenReturn(Optional.empty());
        
        // Mock para el cálculo de promedios: Robot A obtiene 8.0, Robot B obtiene 5.0
        Calificacion cA = new Calificacion(); cA.setPuntaje(8.0);
        Calificacion cB = new Calificacion(); cB.setPuntaje(5.0);
        
        when(calificacionRepository.findByEncuentroAndRobot(encuentro, robotA)).thenReturn(List.of(cA));
        when(calificacionRepository.findByEncuentroAndRobot(encuentro, robotB)).thenReturn(List.of(cB));
        
        // Mock para re-adjuntar el ganador
        when(robotRepository.findById(10L)).thenReturn(Optional.of(robotA)); 
        
        // Act
        torneoService.simularEncuentro(100L);

        // Assert
        // Se debe llamar a save 6 veces (3 jueces * 2 robots)
        verify(calificacionRepository, times(6)).save(any(Calificacion.class));
        
        // Verificar que el encuentro se actualizó con los promedios y el ganador
        assertEquals(8.0, encuentro.getPuntosRobotA());
        assertEquals(5.0, encuentro.getPuntosRobotB());
        assertEquals(robotA, encuentro.getRobotGanador());
        
        verify(encuentroRepository).saveAndFlush(encuentro);
        System.out.println("✓ Test simularEncuentro_Success: Calificaciones generadas y ganador guardado.");
    }

    @Test
    void simularTorneoCompleto_ValidationFails_NoWinner() {
        // Descripción: Verifica que falle si el primer encuentro no tiene ganador (no ha sido calificado).
        when(torneoRepository.findById(1L)).thenReturn(Optional.of(torneo));
        when(encuentroRepository.findByTorneoId(1L)).thenReturn(List.of(encuentro));
        
        encuentro.setRobotGanador(null); // Sin ganador

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> torneoService.simularTorneoCompleto(1L));
        assertTrue(ex.getMessage().contains("debe ser calificado manualmente"));
        System.out.println("✓ Test simularTorneoCompleto_ValidationFails_NoWinner: Validación correcta.");
    }

    @Test
    void simularTorneoCompleto_ValidationFails_NotEnoughJudges() {
        // Descripción: Verifica que falle si el primer encuentro tiene ganador pero menos de 3 jueces distintos calificaron.
        when(torneoRepository.findById(1L)).thenReturn(Optional.of(torneo));
        when(encuentroRepository.findByTorneoId(1L)).thenReturn(List.of(encuentro));
        
        encuentro.setRobotGanador(robotA); // Tiene ganador

        // Mock calificaciones: Solo 2 jueces distintos (juez1 y juez2)
        Calificacion c1 = new Calificacion(); c1.setJuez(juez1);
        Calificacion c2 = new Calificacion(); c2.setJuez(juez2);
        
        when(calificacionRepository.findByEncuentroAndRobot(encuentro, robotA)).thenReturn(List.of(c1));
        when(calificacionRepository.findByEncuentroAndRobot(encuentro, robotB)).thenReturn(List.of(c2));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> torneoService.simularTorneoCompleto(1L));
        assertTrue(ex.getMessage().contains("al menos 3 jueces distintos"));
        System.out.println("✓ Test simularTorneoCompleto_ValidationFails_NotEnoughJudges: Validación de 3 jueces correcta.");
    }

    @Test
    void simularTorneoCompleto_Success() {
        // Descripción: Verifica el flujo exitoso de simulación completa cuando se cumplen los requisitos.
        when(torneoRepository.findById(1L)).thenReturn(Optional.of(torneo));
        
        // Primer encuentro (Manual, ya calificado)
        Encuentro e1 = new Encuentro(); e1.setId(100L); e1.setRobotA(robotA); e1.setRobotB(robotB); e1.setRobotGanador(robotA); e1.setTorneo(torneo);
        // Encuentro pendiente (Para simular)
        Encuentro e2 = new Encuentro(); e2.setId(101L); e2.setRobotA(robotA); e2.setRobotB(robotB); e2.setTorneo(torneo);
        
        when(encuentroRepository.findByTorneoId(1L)).thenReturn(List.of(e1, e2));
        
        // Mock calificaciones para e1 (3 jueces distintos)
        Calificacion c1 = new Calificacion(); c1.setJuez(juez1);
        Calificacion c2 = new Calificacion(); c2.setJuez(juez2);
        Calificacion c3 = new Calificacion(); c3.setJuez(juez3);
        
        when(calificacionRepository.findByEncuentroAndRobot(e1, robotA)).thenReturn(List.of(c1, c2));
        when(calificacionRepository.findByEncuentroAndRobot(e1, robotB)).thenReturn(List.of(c3));

        // Mock para la simulación interna de e2
        when(encuentroRepository.findById(101L)).thenReturn(Optional.of(e2));
        when(robotRepository.findById(anyLong())).thenReturn(Optional.of(robotA)); // Ganador simulado
        
        // Act
        torneoService.simularTorneoCompleto(1L);

        // Assert
        // Verificar que e2 ahora tiene un ganador (fue simulado)
        assertNotNull(e2.getRobotGanador());
        // Verificar que se guardaron calificaciones para e2 (6 veces: 3 jueces * 2 robots)
        verify(calificacionRepository, atLeast(6)).save(any(Calificacion.class));
        System.out.println("✓ Test simularTorneoCompleto_Success: Torneo simulado exitosamente.");
    }
}