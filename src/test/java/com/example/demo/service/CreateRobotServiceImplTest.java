package com.example.demo.service;

import com.example.demo.dto.RobotRequest;
import com.example.demo.model.*;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.CompetidorRepository;
import com.example.demo.repository.RobotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateRobotServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO createRobot()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | createRobot() | Request(nombre="Robot Destructor", peso=9.0, vel=15.0, catId=10) | Robot creado | Verifica persistencia y asignación de datos
     * 2 | createRobot() | Auth(user="unknown@test.com") | Excepción | IllegalArgumentException
     * 3 | createRobot() | Request(catId=999) | Excepción | IllegalArgumentException
     * 4 | createRobot() | Request(peso=15.0) [Max=10.0] | Excepción | IllegalArgumentException
     * 5 | createRobot() | Request(vel=25.0) [Max=20.0] | Excepción | IllegalArgumentException
     * 6 | createRobot() | Request(altura=60.0) [Max=50.0] | Excepción | IllegalArgumentException
     * 7 | createRobot() | Request(ancho=60.0) [Max=50.0] | Excepción | IllegalArgumentException
     */

    @Mock private RobotRepository robotRepository;
    @Mock private CompetidorRepository competidorRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private Authentication authentication;

    @InjectMocks private RobotServiceImpl robotService;

    private RobotRequest request; // Mocked with DEEP_STUBS para manejar DTOs anidados
    private Competidor competidor;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        // Usamos DEEP_STUBS para poder mockear llamadas encadenadas como request.getCaracteristicas().getPesoKg()
        request = mock(RobotRequest.class, RETURNS_DEEP_STUBS);

        competidor = new Competidor();
        competidor.setId(1L);
        competidor.setCorreoElectronico("competidor@test.com");
        Club club = new Club();
        club.setId(10L);
        competidor.setClub(club);

        categoria = new Categoria();
        categoria.setId(10L);
        categoria.setNombre("Sumo");
        categoria.setPesoMaximoKg(10.0);
        categoria.setVelocidadMaximaPermitidaKmh(20.0);
        categoria.setAltoMaximoCm(50.0);
        categoria.setAnchoMaximoCm(50.0);
    }

    @Test
    void createRobot_Success() {
        // Descripción: Verifica que se cree un robot correctamente cuando cumple con todos los requisitos y límites de la categoría.
        System.out.println("TEST: createRobot_Success - Crear robot válido");

        // Arrange
        when(authentication.getName()).thenReturn("competidor@test.com");
        when(competidorRepository.findByCorreoElectronico("competidor@test.com")).thenReturn(Optional.of(competidor));
        when(request.getCategoriaId()).thenReturn(10L);
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria));

        // Configurar valores del request dentro de los límites
        when(request.getNombre()).thenReturn("Robot Destructor");
        when(request.getCaracteristicas().getPesoKg()).thenReturn(9.0); // < 10.0
        when(request.getMovilidad().getVelocidadMaximaKmh()).thenReturn(15.0); // < 20.0
        when(request.getCaracteristicas().getAlturaCm()).thenReturn(40.0); // < 50.0
        when(request.getCaracteristicas().getAnchoCm()).thenReturn(40.0); // < 50.0
        
        // Mock save
        when(robotRepository.save(any(Robot.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Robot result = robotService.createRobot(request, authentication);

        // Assert
        assertNotNull(result);
        assertEquals("Robot Destructor", result.getNombre());
        assertEquals(categoria, result.getCategoria());
        assertEquals(competidor, result.getCompetidor());
        assertEquals(RobotEstado.ACTIVO, result.getEstado());
        verify(robotRepository).save(any(Robot.class));
    }

    @Test
    void createRobot_CompetidorNotFound_Throws() {
        // Descripción: Verifica que se lance excepción si el competidor autenticado no existe en la BD.
        System.out.println("TEST: createRobot_CompetidorNotFound_Throws - Competidor no encontrado");

        when(authentication.getName()).thenReturn("unknown@test.com");
        when(competidorRepository.findByCorreoElectronico("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> robotService.createRobot(request, authentication));
    }

    @Test
    void createRobot_CategoriaNotFound_Throws() {
        // Descripción: Verifica que se lance excepción si la categoría solicitada no existe.
        System.out.println("TEST: createRobot_CategoriaNotFound_Throws - Categoría no encontrada");

        when(authentication.getName()).thenReturn("competidor@test.com");
        when(competidorRepository.findByCorreoElectronico("competidor@test.com")).thenReturn(Optional.of(competidor));
        when(request.getCategoriaId()).thenReturn(999L);
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> robotService.createRobot(request, authentication));
    }

    @Test
    void createRobot_WeightExceeded_Throws() {
        // Descripción: Verifica que se lance excepción si el peso del robot supera el máximo de la categoría.
        System.out.println("TEST: createRobot_WeightExceeded_Throws - Peso excede límite");

        when(authentication.getName()).thenReturn("competidor@test.com");
        when(competidorRepository.findByCorreoElectronico("competidor@test.com")).thenReturn(Optional.of(competidor));
        when(request.getCategoriaId()).thenReturn(10L);
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria));

        // Peso 15 > 10
        when(request.getCaracteristicas().getPesoKg()).thenReturn(15.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> robotService.createRobot(request, authentication));
        assertTrue(ex.getMessage().contains("peso"));
    }

    @Test
    void createRobot_VelocityExceeded_Throws() {
        // Descripción: Verifica que se lance excepción si la velocidad del robot supera el máximo de la categoría.
        System.out.println("TEST: createRobot_VelocityExceeded_Throws - Velocidad excede límite");

        when(authentication.getName()).thenReturn("competidor@test.com");
        when(competidorRepository.findByCorreoElectronico("competidor@test.com")).thenReturn(Optional.of(competidor));
        when(request.getCategoriaId()).thenReturn(10L);
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria));

        // Velocidad 25 > 20
        when(request.getMovilidad().getVelocidadMaximaKmh()).thenReturn(25.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> robotService.createRobot(request, authentication));
        assertTrue(ex.getMessage().contains("velocidad"));
    }

    @Test
    void createRobot_HeightExceeded_Throws() {
        // Descripción: Verifica que se lance excepción si la altura del robot supera el máximo de la categoría.
        System.out.println("TEST: createRobot_HeightExceeded_Throws - Altura excede límite");

        when(authentication.getName()).thenReturn("competidor@test.com");
        when(competidorRepository.findByCorreoElectronico("competidor@test.com")).thenReturn(Optional.of(competidor));
        when(request.getCategoriaId()).thenReturn(10L);
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria));

        // Altura 60 > 50
        when(request.getCaracteristicas().getAlturaCm()).thenReturn(60.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> robotService.createRobot(request, authentication));
        assertTrue(ex.getMessage().contains("altura"));
    }

    @Test
    void createRobot_WidthExceeded_Throws() {
        // Descripción: Verifica que se lance excepción si el ancho del robot supera el máximo de la categoría.
        System.out.println("TEST: createRobot_WidthExceeded_Throws - Ancho excede límite");

        when(authentication.getName()).thenReturn("competidor@test.com");
        when(competidorRepository.findByCorreoElectronico("competidor@test.com")).thenReturn(Optional.of(competidor));
        when(request.getCategoriaId()).thenReturn(10L);
        when(categoriaRepository.findById(10L)).thenReturn(Optional.of(categoria));

        // Ancho 60 > 50
        when(request.getCaracteristicas().getAnchoCm()).thenReturn(60.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> robotService.createRobot(request, authentication));
        assertTrue(ex.getMessage().contains("ancho"));
    }
}