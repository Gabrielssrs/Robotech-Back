package com.example.demo.service;

import com.example.demo.dto.TorneoRequest;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TorneoServiceImplTest {

    @Mock
    private TorneoRepository torneoRepository;
    @Mock
    private SedeRepository sedeRepository;
    @Mock
    private RobotRepository robotRepository;
    @Mock
    private EncuentroRepository encuentroRepository;
    // Mocks necesarios aunque no se usen directamente en todos los tests para evitar NullPointer en el servicio
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private JuezRepository juezRepository;
    @Mock private CalificacionRepository calificacionRepository;
    @Mock private ResultadoTorneoRepository resultadoTorneoRepository;

    @InjectMocks
    private TorneoServiceImpl torneoService;

    // --- PRUEBAS PARA CREAR TORNEO (SAMIR) ---

    @Test
    void crearTorneo_Exitoso() {
        // Arrange
        TorneoRequest request = new TorneoRequest();
        request.setNombre("Torneo Nacional 2024");
        request.setSedeId(1L);
        request.setFechaInicio(LocalDate.now().plusDays(10));
        // Configuración simplificada de fechas manuales

        Sede sedeMock = new Sede();
        sedeMock.setId(1L);

        when(torneoRepository.existsByNombre(request.getNombre())).thenReturn(false);
        when(sedeRepository.findById(1L)).thenReturn(Optional.of(sedeMock));
        when(torneoRepository.save(any(Torneo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Torneo resultado = torneoService.createTorneo(request);

        // Assert
        assertNotNull(resultado);
        assertEquals("Torneo Nacional 2024", resultado.getNombre());
        assertEquals(TorneoEstado.PROXIMAMENTE, resultado.getEstado());
        verify(torneoRepository).save(any(Torneo.class));
        System.out.println("Test crearTorneo_Exitoso pasó correctamente.");
    }

    @Test
    void crearTorneo_NombreDuplicado_LanzaExcepcion() {
        // Arrange
        TorneoRequest request = new TorneoRequest();
        request.setNombre("Torneo Existente");

        when(torneoRepository.existsByNombre("Torneo Existente")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> torneoService.createTorneo(request));
        // Nota: El servicio envuelve la IllegalArgumentException en una RuntimeException
        assertTrue(exception.getMessage().contains("El nombre del torneo ya existe"));
        System.out.println("Test crearTorneo_NombreDuplicado pasó correctamente.");
    }

    // --- PRUEBAS PARA INSCRIPCIÓN (GENERAL) ---

    @Test
    void inscribirRobot_Exitoso() {
        // Arrange
        Long torneoId = 1L;
        Long robotId = 100L;

        Categoria categoriaLigera = new Categoria();
        categoriaLigera.setId(5L);

        Torneo torneo = new Torneo();
        torneo.setId(torneoId);
        torneo.setEstado(TorneoEstado.PROXIMAMENTE);
        torneo.getCategorias().add(categoriaLigera);

        Club clubA = new Club(); clubA.setId(10L);
        Robot robot = new Robot();
        robot.setId(robotId);
        robot.setCategoria(categoriaLigera);
        robot.setClub(clubA);

        when(torneoRepository.findById(torneoId)).thenReturn(Optional.of(torneo));
        when(robotRepository.findById(robotId)).thenReturn(Optional.of(robot));

        // Act
        torneoService.inscribirRobot(torneoId, robotId);

        // Assert
        assertTrue(torneo.getParticipantes().contains(robot));
        verify(torneoRepository).save(torneo);
        System.out.println("Test inscribirRobot_Exitoso pasó correctamente.");
    }

    @Test
    void inscribirRobot_CategoriaIncorrecta_LanzaExcepcion() {
        // Arrange
        Long torneoId = 1L;
        Long robotId = 100L;

        Categoria categoriaTorneo = new Categoria(); categoriaTorneo.setId(1L);
        Categoria categoriaRobot = new Categoria(); categoriaRobot.setId(2L); // Diferente

        Torneo torneo = new Torneo();
        torneo.setId(torneoId);
        torneo.setEstado(TorneoEstado.PROXIMAMENTE);
        torneo.getCategorias().add(categoriaTorneo);

        Robot robot = new Robot();
        robot.setId(robotId);
        robot.setCategoria(categoriaRobot);

        when(torneoRepository.findById(torneoId)).thenReturn(Optional.of(torneo));
        when(robotRepository.findById(robotId)).thenReturn(Optional.of(robot));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> torneoService.inscribirRobot(torneoId, robotId));
        System.out.println("Test inscribirRobot_CategoriaIncorrecta pasó correctamente.");
    }

    @Test
    void inscribirRobot_TorneoLleno_LanzaExcepcion() {
        // Arrange
        Torneo torneo = new Torneo();
        torneo.setEstado(TorneoEstado.PROXIMAMENTE);
        // Simulamos que ya tiene 16 participantes
        for(int i=0; i<16; i++) torneo.getParticipantes().add(new Robot());

        when(torneoRepository.findById(1L)).thenReturn(Optional.of(torneo));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> torneoService.inscribirRobot(1L, 100L));
        System.out.println("Test inscribirRobot_TorneoLleno pasó correctamente.");
    }
}