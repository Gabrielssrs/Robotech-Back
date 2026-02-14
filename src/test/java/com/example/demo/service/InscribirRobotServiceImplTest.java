package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscribirRobotServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO inscribirRobot()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | inscribirRobot() | TorneoId=100, RobotId=10 (Válidos) | Inscripción exitosa | Robot añadido a participantes
     * 2 | inscribirRobot() | TorneoId=999 (No existe) | Excepción | IllegalArgumentException
     * 3 | inscribirRobot() | Torneo Estado=EN_CURSO | Excepción | IllegalStateException
     * 4 | inscribirRobot() | Torneo Estado=CANCELADO | Excepción | IllegalStateException
     * 5 | inscribirRobot() | Torneo Lleno (16 participantes) | Excepción | IllegalStateException
     * 6 | inscribirRobot() | RobotId=999 (No existe) | Excepción | IllegalArgumentException
     * 7 | inscribirRobot() | Categoría Robot != Categoría Torneo | Excepción | IllegalArgumentException
     * 8 | inscribirRobot() | Club ya tiene 2 robots inscritos | Excepción | IllegalStateException
     * 9 | inscribirRobot() | Robot ya inscrito | Excepción | IllegalStateException
     * 10 | inscribirRobot() | Inscripción del participante 16 | Fixture generado | Estado cambia a EN_CURSO
     */

    @Mock private TorneoRepository torneoRepository;
    @Mock private RobotRepository robotRepository;
    @Mock private EncuentroRepository encuentroRepository;
    
    // Mocks necesarios para el constructor de TorneoServiceImpl
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private JuezRepository juezRepository;
    @Mock private SedeRepository sedeRepository;
    @Mock private CalificacionRepository calificacionRepository;
    @Mock private ResultadoTorneoRepository resultadoTorneoRepository;

    @InjectMocks
    private TorneoServiceImpl torneoService;

    private Torneo torneo;
    private Robot robot;
    private Categoria categoria;
    private Club club;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Sumo");

        club = new Club();
        club.setId(10L);
        club.setNombre("Club Alpha");

        torneo = new Torneo();
        torneo.setId(100L);
        torneo.setNombre("Torneo Test");
        torneo.setEstado(TorneoEstado.PROXIMAMENTE);
        torneo.setCategorias(new HashSet<>(Set.of(categoria)));
        torneo.setParticipantes(new HashSet<>());

        robot = new Robot();
        robot.setId(10L);
        robot.setNombre("Robot X");
        robot.setCategoria(categoria);
        robot.setClub(club);
    }

    @Test
    void inscribirRobot_Success() {
        // Descripción: Verifica que un robot válido se inscriba correctamente en un torneo disponible.
        when(torneoRepository.findById(100L)).thenReturn(Optional.of(torneo));
        when(robotRepository.findById(10L)).thenReturn(Optional.of(robot));
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(i -> i.getArguments()[0]);

        torneoService.inscribirRobot(100L, 10L);

        assertTrue(torneo.getParticipantes().contains(robot));
        verify(torneoRepository).save(torneo);
        System.out.println("✓ Caso 1: Inscripción exitosa - EXITOSO");
    }

    @Test
    void inscribirRobot_TorneoNotFound_Throws() {
        // Descripción: Verifica que lance excepción si el torneo no existe.
        when(torneoRepository.findById(999L)).thenReturn(Optional.empty());
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> torneoService.inscribirRobot(999L, 10L));
        assertTrue(ex.getMessage().contains("Torneo no encontrado"));
        System.out.println("✓ Caso 2: Torneo no encontrado - RECHAZADO");
    }

    @Test
    void inscribirRobot_WrongState_Throws() {
        // Descripción: Verifica que lance excepción si el torneo no está en etapa de inscripción.
        torneo.setEstado(TorneoEstado.EN_CURSO);
        when(torneoRepository.findById(100L)).thenReturn(Optional.of(torneo));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> torneoService.inscribirRobot(100L, 10L));
        assertTrue(ex.getMessage().contains("etapa de inscripción"));
        System.out.println("✓ Caso 3: Torneo en curso - RECHAZADO");
    }

    @Test
    void inscribirRobot_CancelledState_Throws() {
        // Descripción: Verifica que lance excepción si el torneo está cancelado.
        torneo.setEstado(TorneoEstado.CANCELADO);
        when(torneoRepository.findById(100L)).thenReturn(Optional.of(torneo));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> torneoService.inscribirRobot(100L, 10L));
        assertTrue(ex.getMessage().contains("cancelado"));
        System.out.println("✓ Caso 4: Torneo cancelado - RECHAZADO");
    }

    @Test
    void inscribirRobot_TorneoFull_Throws() {
        // Descripción: Verifica que lance excepción si el torneo ya tiene 16 participantes.
        for (int i = 0; i < 16; i++) {
            torneo.getParticipantes().add(new Robot());
        }
        when(torneoRepository.findById(100L)).thenReturn(Optional.of(torneo));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> torneoService.inscribirRobot(100L, 10L));
        assertTrue(ex.getMessage().contains("límite de 16 participantes"));
        System.out.println("✓ Caso 5: Torneo lleno - RECHAZADO");
    }

    @Test
    void inscribirRobot_RobotNotFound_Throws() {
        // Descripción: Verifica que lance excepción si el robot no existe.
        when(torneoRepository.findById(100L)).thenReturn(Optional.of(torneo));
        when(robotRepository.findById(999L)).thenReturn(Optional.empty());
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> torneoService.inscribirRobot(100L, 999L));
        assertTrue(ex.getMessage().contains("Robot no encontrado"));
        System.out.println("✓ Caso 6: Robot no encontrado - RECHAZADO");
    }

    @Test
    void inscribirRobot_CategoryMismatch_Throws() {
        // Descripción: Verifica que lance excepción si la categoría del robot no coincide con las del torneo.
        Categoria otraCategoria = new Categoria();
        otraCategoria.setId(2L);
        otraCategoria.setNombre("Mini Sumo");
        robot.setCategoria(otraCategoria); // Robot es de otra categoría

        when(torneoRepository.findById(100L)).thenReturn(Optional.of(torneo));
        when(robotRepository.findById(10L)).thenReturn(Optional.of(robot));
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> torneoService.inscribirRobot(100L, 10L));
        assertTrue(ex.getMessage().contains("no pertenece a ninguna de las categorías"));
        System.out.println("✓ Caso 7: Categoría incorrecta - RECHAZADO");
    }

    @Test
    void inscribirRobot_ClubLimitReached_Throws() {
        // Descripción: Verifica que lance excepción si el club ya tiene 2 robots inscritos.
        Robot r1 = new Robot(); r1.setId(11L); r1.setClub(club);
        Robot r2 = new Robot(); r2.setId(12L); r2.setClub(club);
        torneo.getParticipantes().add(r1);
        torneo.getParticipantes().add(r2);

        when(torneoRepository.findById(100L)).thenReturn(Optional.of(torneo));
        when(robotRepository.findById(10L)).thenReturn(Optional.of(robot));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> torneoService.inscribirRobot(100L, 10L));
        assertTrue(ex.getMessage().contains("límite máximo de 2 robots"));
        System.out.println("✓ Caso 8: Límite de club alcanzado - RECHAZADO");
    }

    @Test
    void inscribirRobot_AlreadyRegistered_Throws() {
        // Descripción: Verifica que lance excepción si el robot ya está inscrito.
        torneo.getParticipantes().add(robot);

        when(torneoRepository.findById(100L)).thenReturn(Optional.of(torneo));
        when(robotRepository.findById(10L)).thenReturn(Optional.of(robot));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, 
            () -> torneoService.inscribirRobot(100L, 10L));
        assertTrue(ex.getMessage().contains("ya está inscrito"));
        System.out.println("✓ Caso 9: Robot ya inscrito - RECHAZADO");
    }

    @Test
    void inscribirRobot_FixtureGeneration_Success() {
        // Descripción: Verifica que al inscribir el participante número 16, se genere el fixture y cambie el estado.
        for (int i = 0; i < 15; i++) {
            Robot r = new Robot();
            r.setId((long) (20 + i));
            r.setClub(new Club()); // Clubs diferentes para no activar límite
            r.getClub().setId((long) (50 + i));
            torneo.getParticipantes().add(r);
        }

        when(torneoRepository.findById(100L)).thenReturn(Optional.of(torneo));
        when(robotRepository.findById(10L)).thenReturn(Optional.of(robot));
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(i -> i.getArguments()[0]);

        torneoService.inscribirRobot(100L, 10L);

        assertEquals(16, torneo.getParticipantes().size());
        assertEquals(TorneoEstado.EN_CURSO, torneo.getEstado());
        verify(encuentroRepository, times(8)).save(any(Encuentro.class)); // 8 encuentros generados
        System.out.println("✓ Caso 10: Generación de Fixture (16 participantes) - EXITOSO");
    }
}