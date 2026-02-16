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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetEstadoJuecesServiceImplTest {

    @Mock private EncuentroRepository encuentroRepository;
    @Mock private CalificacionRepository calificacionRepository;
    
    // Mocks adicionales necesarios para que @InjectMocks pueda instanciar el servicio
    @Mock private TorneoRepository torneoRepository;
    @Mock private RobotRepository robotRepository;
    @Mock private JuezRepository juezRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private SedeRepository sedeRepository;
    @Mock private ResultadoTorneoRepository resultadoTorneoRepository;

    @InjectMocks
    private TorneoServiceImpl torneoService;

    private Encuentro encuentro;
    private Torneo torneo;
    private Juez juezListo, juezPendiente;
    private Robot robotA, robotB;

    @BeforeEach
    void setUp() {
        // Configurar Jueces
        juezListo = new Juez(); juezListo.setId(1L); juezListo.setNombre("Juez Rápido");
        juezPendiente = new Juez(); juezPendiente.setId(2L); juezPendiente.setNombre("Juez Lento");

        // Configurar Torneo con los jueces
        torneo = new Torneo();
        torneo.setJueces(new HashSet<>(Set.of(juezListo, juezPendiente)));

        // Configurar Robots
        robotA = new Robot(); robotA.setId(10L);
        robotB = new Robot(); robotB.setId(20L);

        // Configurar Encuentro
        encuentro = new Encuentro();
        encuentro.setId(100L);
        encuentro.setTorneo(torneo);
        encuentro.setRobotA(robotA);
        encuentro.setRobotB(robotB);
    }

    @Test
    void getEstadoJueces_ReturnsCorrectStatus() {
        // Descripción: Verifica que el servicio identifique correctamente quién ha terminado de calificar.
        
        when(encuentroRepository.findById(100L)).thenReturn(Optional.of(encuentro));

        // CASO 1: Juez Listo (Calificó a Robot A y Robot B) -> Debe retornar TRUE
        when(calificacionRepository.findByEncuentroAndRobotAndJuez(encuentro, robotA, juezListo)).thenReturn(Optional.of(new Calificacion()));
        when(calificacionRepository.findByEncuentroAndRobotAndJuez(encuentro, robotB, juezListo)).thenReturn(Optional.of(new Calificacion()));

        // CASO 2: Juez Pendiente (Calificó solo a Robot A, falta B) -> Debe retornar FALSE
        when(calificacionRepository.findByEncuentroAndRobotAndJuez(encuentro, robotA, juezPendiente)).thenReturn(Optional.of(new Calificacion()));
        when(calificacionRepository.findByEncuentroAndRobotAndJuez(encuentro, robotB, juezPendiente)).thenReturn(Optional.empty());

        // Ejecutar
        List<Map<String, Object>> result = torneoService.getEstadoJueces(100L);

        // Verificar
        assertEquals(2, result.size());
        
        // Verificar estado del Juez Listo
        Map<String, Object> statusJuez1 = result.stream().filter(m -> m.get("juezId").equals(1L)).findFirst().orElseThrow();
        assertEquals("Juez Rápido", statusJuez1.get("nombre"));
        assertEquals(true, statusJuez1.get("listo"));

        // Verificar estado del Juez Pendiente
        Map<String, Object> statusJuez2 = result.stream().filter(m -> m.get("juezId").equals(2L)).findFirst().orElseThrow();
        assertEquals("Juez Lento", statusJuez2.get("nombre"));
        assertEquals(false, statusJuez2.get("listo"));
    }
    
    @Test
    void getEstadoJueces_EncuentroNotFound_Throws() {
        when(encuentroRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> torneoService.getEstadoJueces(999L));
    }
}