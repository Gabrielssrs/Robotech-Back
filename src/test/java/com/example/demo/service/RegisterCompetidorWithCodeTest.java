package com.example.demo.service;

import com.example.demo.dto.CompetidorRegistroRequest;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterCompetidorWithCodeTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO registrarConCodigo()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | registrarConCodigo() | Código válido, correos coinciden | Competidor creado | Código se marca usado, competidor se guarda
     * 2 | registrarConCodigo() | Correos NO coinciden | Excepción SecurityException | Validación de seguridad crítica
     * 3 | registrarConCodigo() | Código expirado | Excepción IllegalStateException | Fecha expiración < hoy
     * 4 | registrarConCodigo() | Código ya usado | Excepción IllegalStateException | isUtilizado = true
     * 5 | registrarConCodigo() | Código no existe | Excepción IllegalArgumentException | Código no encontrado en BD
     */

    @Mock private CompetidorRepository competidorRepository;
    @Mock private RolRepository rolRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private CodigoAceptacionRepository codigoAceptacionRepository;
    @Mock private CodigoRetiroRepository codigoRetiroRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ImageUploadService imageUploadService;

    @InjectMocks
    private CompetidorServiceImpl competidorService;

    private CompetidorRegistroRequest request;
    private CodigoAceptacion codigoAceptacion;
    private Solicitud solicitudOriginal;
    private Club club;

    @BeforeEach
    void setUp() {
        club = new Club();
        club.setId(1L);
        club.setNombre("Club Robot");

        solicitudOriginal = new Solicitud();
        solicitudOriginal.setId(10L);
        solicitudOriginal.setCorreoElectronico("competidor@test.com"); // Correo original
        solicitudOriginal.setClub(club);

        codigoAceptacion = new CodigoAceptacion();
        codigoAceptacion.setId(50L);
        codigoAceptacion.setCodigo("ABC1234");
        codigoAceptacion.setFechaExpiracion(LocalDate.now().plusDays(1)); // Válido
        codigoAceptacion.setUtilizado(false);
        codigoAceptacion.setSolicitud(solicitudOriginal);

        request = new CompetidorRegistroRequest();
        request.setCodigo("ABC1234");
        request.setCorreoElectronico("competidor@test.com"); // Coincide
        request.setNombre("Juan");
        request.setApellido("Perez");
        request.setDni("12345678");
        request.setTelefono("999888777");
        request.setContrasena("password123");
    }

    @Test
    void registrarConCodigo_Success() {
        System.out.println("TEST: registrarConCodigo_Success - Registro exitoso con código válido");

        when(codigoAceptacionRepository.findByCodigo("ABC1234")).thenReturn(Optional.of(codigoAceptacion));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");
        
        Rol rolCompetidor = new Rol();
        rolCompetidor.setRol(RolNombre.ROLE_COMPETIDOR);
        when(rolRepository.findByRol(RolNombre.ROLE_COMPETIDOR)).thenReturn(Optional.of(rolCompetidor));
        
        when(competidorRepository.save(any(Competidor.class))).thenAnswer(i -> i.getArguments()[0]);

        Competidor result = competidorService.registrarConCodigo(request);

        assertNotNull(result);
        assertEquals("competidor@test.com", result.getCorreoElectronico());
        assertEquals(club, result.getClub()); // Hereda el club de la solicitud
        assertTrue(codigoAceptacion.isUtilizado()); // El código debe quedar marcado como usado
        verify(codigoAceptacionRepository).save(codigoAceptacion);
        verify(competidorRepository).save(any(Competidor.class));
    }

    @Test
    void registrarConCodigo_EmailMismatch_ThrowsSecurityException() {
        System.out.println("TEST: registrarConCodigo_EmailMismatch_ThrowsSecurityException - Correo diferente al de la solicitud");
        
        // El request intenta usar el código con otro correo
        request.setCorreoElectronico("hacker@test.com");

        when(codigoAceptacionRepository.findByCodigo("ABC1234")).thenReturn(Optional.of(codigoAceptacion));

        SecurityException ex = assertThrows(SecurityException.class, () -> competidorService.registrarConCodigo(request));
        assertTrue(ex.getMessage().contains("El correo electrónico no coincide"));
        
        // Verificar que NO se marcó como usado ni se guardó competidor
        assertFalse(codigoAceptacion.isUtilizado());
        verify(competidorRepository, never()).save(any(Competidor.class));
    }

    @Test
    void registrarConCodigo_CodeExpired_ThrowsIllegalStateException() {
        System.out.println("TEST: registrarConCodigo_CodeExpired_ThrowsIllegalStateException - Código expirado");
        
        codigoAceptacion.setFechaExpiracion(LocalDate.now().minusDays(1)); // Expiró ayer

        when(codigoAceptacionRepository.findByCodigo("ABC1234")).thenReturn(Optional.of(codigoAceptacion));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> competidorService.registrarConCodigo(request));
        assertTrue(ex.getMessage().contains("expirado"));
    }

    @Test
    void registrarConCodigo_CodeAlreadyUsed_ThrowsIllegalStateException() {
        System.out.println("TEST: registrarConCodigo_CodeAlreadyUsed_ThrowsIllegalStateException - Código ya utilizado");
        
        codigoAceptacion.setUtilizado(true); // Ya usado

        when(codigoAceptacionRepository.findByCodigo("ABC1234")).thenReturn(Optional.of(codigoAceptacion));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> competidorService.registrarConCodigo(request));
        assertTrue(ex.getMessage().contains("ya ha sido utilizado"));
    }
}