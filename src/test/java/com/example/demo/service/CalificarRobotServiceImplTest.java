package com.example.demo.service;

import com.example.demo.dto.CalificacionRequest;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalificarRobotServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO calificarRobot()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | calificarRobot() | Request(encuentroId=1, robotId=10, puntaje=9.5), Juez="juez@test.com" | Calificación guardada, Encuentro actualizado | Flujo exitoso (nueva calificación)
     * 2 | calificarRobot() | Juez email no existe | Excepción | IllegalArgumentException
     * 3 | calificarRobot() | Encuentro ID no existe | Excepción | IllegalArgumentException
     * 4 | calificarRobot() | Robot ID no existe | Excepción | IllegalArgumentException
     * 5 | calificarRobot() | Robot no participa en el encuentro | Excepción | IllegalArgumentException
     * 6 | calificarRobot() | Calificación ya existe (actualización) | Calificación actualizada | Verifica que no duplica, sino actualiza
     */

    @Mock private TorneoRepository torneoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private JuezRepository juezRepository;
    @Mock private SedeRepository sedeRepository;
    @Mock private RobotRepository robotRepository;
    @Mock private EncuentroRepository encuentroRepository;
    @Mock private CalificacionRepository calificacionRepository;
    @Mock private ResultadoTorneoRepository resultadoTorneoRepository;

    @InjectMocks
    private TorneoServiceImpl torneoService;

    private Juez juez;
    private Robot robotA;
    private Robot robotB;
    private Encuentro encuentro;
    private CalificacionRequest request;

    @BeforeEach
    void setUp() {
        juez = new Juez();
        juez.setId(1L);
        juez.setCorreo("juez@test.com");

        robotA = new Robot();
        robotA.setId(10L);
        robotA.setNombre("Robot A");

        robotB = new Robot();
        robotB.setId(20L);
        robotB.setNombre("Robot B");

        encuentro = new Encuentro();
        encuentro.setId(100L);
        encuentro.setRobotA(robotA);
        encuentro.setRobotB(robotB);

        request = new CalificacionRequest();
        request.setEncuentroId(100L);
        request.setRobotId(10L); // Calificar a Robot A
        request.setPuntaje(9.5);
    }

    @Test
    void calificarRobot_Success_NewScore() {
        // Descripción: Verifica que se registre una nueva calificación correctamente y se actualice el promedio en el encuentro.
        
        when(juezRepository.findByCorreo("juez@test.com")).thenReturn(Optional.of(juez));
        when(encuentroRepository.findById(100L)).thenReturn(Optional.of(encuentro));
        when(robotRepository.findById(10L)).thenReturn(Optional.of(robotA));
        
        // No existe calificación previa
        when(calificacionRepository.findByEncuentroAndRobotAndJuez(encuentro, robotA, juez)).thenReturn(Optional.empty());
        
        // Mock save
        when(calificacionRepository.save(any(Calificacion.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // Mock lista de calificaciones para promedio (simulamos que devuelve la que acabamos de guardar)
        Calificacion califGuardada = new Calificacion();
        califGuardada.setPuntaje(9.5);
        when(calificacionRepository.findByEncuentroAndRobot(encuentro, robotA)).thenReturn(List.of(califGuardada));

        torneoService.calificarRobot("juez@test.com", request);

        verify(calificacionRepository).save(any(Calificacion.class));
        verify(encuentroRepository).save(encuentro);
        assertEquals(9.5, encuentro.getPuntosRobotA()); // Verifica que se actualizó el puntaje en el encuentro
        
        System.out.println("✓ Caso 1: Calificación nueva exitosa - EXITOSO");
    }

    @Test
    void calificarRobot_JuezNotFound_Throws() {
        // Descripción: Verifica que lance excepción si el juez no existe.
        
        when(juezRepository.findByCorreo("unknown@test.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> torneoService.calificarRobot("unknown@test.com", request));
        
        assertTrue(ex.getMessage().contains("Juez no encontrado"));
        System.out.println("✓ Caso 2: Juez no encontrado - RECHAZADO");
    }

    @Test
    void calificarRobot_EncuentroNotFound_Throws() {
        // Descripción: Verifica que lance excepción si el encuentro no existe.
        
        when(juezRepository.findByCorreo("juez@test.com")).thenReturn(Optional.of(juez));
        when(encuentroRepository.findById(100L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> torneoService.calificarRobot("juez@test.com", request));
        
        assertTrue(ex.getMessage().contains("Encuentro no encontrado"));
        System.out.println("✓ Caso 3: Encuentro no encontrado - RECHAZADO");
    }

    @Test
    void calificarRobot_RobotNotFound_Throws() {
        // Descripción: Verifica que lance excepción si el robot no existe.
        
        when(juezRepository.findByCorreo("juez@test.com")).thenReturn(Optional.of(juez));
        when(encuentroRepository.findById(100L)).thenReturn(Optional.of(encuentro));
        when(robotRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> torneoService.calificarRobot("juez@test.com", request));
        
        assertTrue(ex.getMessage().contains("Robot no encontrado"));
        System.out.println("✓ Caso 4: Robot no encontrado - RECHAZADO");
    }

    @Test
    void calificarRobot_RobotNotInMatch_Throws() {
        // Descripción: Verifica que lance excepción si el robot no participa en el encuentro.
        
        Robot robotC = new Robot();
        robotC.setId(30L); // Robot ajeno

        request.setRobotId(30L);

        when(juezRepository.findByCorreo("juez@test.com")).thenReturn(Optional.of(juez));
        when(encuentroRepository.findById(100L)).thenReturn(Optional.of(encuentro));
        when(robotRepository.findById(30L)).thenReturn(Optional.of(robotC));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> torneoService.calificarRobot("juez@test.com", request));
        
        assertTrue(ex.getMessage().contains("El robot no participa"));
        System.out.println("✓ Caso 5: Robot no participa en encuentro - RECHAZADO");
    }

    @Test
    void calificarRobot_UpdateExistingScore_Success() {
        // Descripción: Verifica que si ya existe una calificación, se actualice en lugar de crear una nueva.
        
        when(juezRepository.findByCorreo("juez@test.com")).thenReturn(Optional.of(juez));
        when(encuentroRepository.findById(100L)).thenReturn(Optional.of(encuentro));
        when(robotRepository.findById(10L)).thenReturn(Optional.of(robotA));

        Calificacion existingCalificacion = new Calificacion();
        existingCalificacion.setId(500L);
        existingCalificacion.setPuntaje(5.0); // Puntaje anterior

        when(calificacionRepository.findByEncuentroAndRobotAndJuez(encuentro, robotA, juez))
                .thenReturn(Optional.of(existingCalificacion));
        
        when(calificacionRepository.save(any(Calificacion.class))).thenAnswer(i -> i.getArguments()[0]);

        // Simulamos que al buscar todas las calificaciones, ahora el promedio refleja el cambio (9.5)
        Calificacion updatedCalificacion = new Calificacion();
        updatedCalificacion.setPuntaje(9.5);
        when(calificacionRepository.findByEncuentroAndRobot(encuentro, robotA)).thenReturn(List.of(updatedCalificacion));

        torneoService.calificarRobot("juez@test.com", request);

        verify(calificacionRepository).save(existingCalificacion); // Debe guardar la misma instancia
        assertEquals(9.5, existingCalificacion.getPuntaje()); // El puntaje debe haber cambiado
        System.out.println("✓ Caso 6: Actualización de calificación existente - EXITOSO");
    }
}