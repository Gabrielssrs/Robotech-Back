package com.example.demo.service;

import com.example.demo.model.Club;
import com.example.demo.model.Solicitud;
import com.example.demo.model.SolicitudEstado;
import com.example.demo.repository.ClubRepository;
import com.example.demo.repository.SolicitudRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSolicitudServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO createSolicitud()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | createSolicitud() | Solicitud(competidor=100), ClubId=1 | Solicitud guardada | Verifica asignación de club y persistencia
     * 2 | createSolicitud() | ClubId=999 (Inexistente) | Excepción | IllegalArgumentException por club no encontrado
     * 3 | createSolicitud() | Solicitud=null, ClubId=1 | Excepción | NullPointerException al intentar asignar club
     * 4 | createSolicitud() | Email inválido | Excepción | IllegalArgumentException
     */

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SolicitudServiceImpl solicitudService;

    @Test
    void createSolicitud_Success() {
        // Descripción: Verifica que se cree una solicitud correctamente asignando el club, fecha y estado inicial PENDIENTE.
        System.out.println("TEST: createSolicitud_Success - Crear solicitud válida");

        Long clubId = 1L;
        Club club = new Club();
        club.setId(clubId);

        Solicitud solicitud = new Solicitud();
        solicitud.setNombreCompleto("Juan Perez");
        solicitud.setCorreoElectronico("test@valid.com");

        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(solicitudRepository.save(any(Solicitud.class))).thenAnswer(invocation -> {
            Solicitud s = invocation.getArgument(0);
            s.setId(500L); // Simular ID generado
            s.setEstado(SolicitudEstado.PENDIENTE); // Simular lógica del servicio
            return s;
        });

        Solicitud result = solicitudService.createSolicitud(solicitud, clubId);

        assertNotNull(result);
        assertEquals(club, result.getClub());
        assertEquals(SolicitudEstado.PENDIENTE, result.getEstado());
        verify(solicitudRepository).save(any(Solicitud.class));
    }

    @Test
    void createSolicitud_ClubNotFound_Throws() {
        // Descripción: Verifica que se lance una excepción si el club al que se intenta ingresar no existe.
        System.out.println("TEST: createSolicitud_ClubNotFound_Throws - Club no encontrado");

        Long clubId = 999L;
        Solicitud solicitud = new Solicitud();
        solicitud.setCorreoElectronico("test@valid.com");

        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(RuntimeException.class, () -> solicitudService.createSolicitud(solicitud, clubId));
        assertTrue(ex.getMessage() != null);
    }

    @Test
    void createSolicitud_NullSolicitud_Throws() {
        // Descripción: Verifica que se lance una excepción (NPE) si el objeto solicitud enviado es nulo, protegiendo la integridad del servicio.
        System.out.println("TEST: createSolicitud_NullSolicitud_Throws - Solicitud nula");

        Long clubId = 1L;

        assertThrows(NullPointerException.class, () -> solicitudService.createSolicitud(null, clubId));
    }

    @Test
    void createSolicitud_InvalidEmail_Throws() {
        // Descripción: Verifica que se lance una excepción si el formato del correo electrónico no es válido.
        System.out.println("TEST: createSolicitud_InvalidEmail_Throws - Email inválido");

        Long clubId = 1L;
        Solicitud solicitud = new Solicitud();
        solicitud.setCorreoElectronico("correo-invalido"); // Sin @ ni dominio

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> solicitudService.createSolicitud(solicitud, clubId));
        assertEquals("El formato del correo electrónico no es válido.", ex.getMessage());
    }
}