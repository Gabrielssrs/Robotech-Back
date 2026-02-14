package com.example.demo.service;

import com.example.demo.model.Club;
import com.example.demo.model.Rol;
import com.example.demo.model.RolNombre;
import com.example.demo.repository.ClubRepository;
import com.example.demo.repository.RolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateClubServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO createClub()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | createClub() | Request(nombre="ClubTest", correo="club@test.com", tel="900900900", pass="PlainPwd1!") | Club creado | Se encripta contraseña y asigna rol
     * 2 | createClub() | Request(nombre="ClubTest") [Duplicado] | Excepción | IllegalStateException
     * 3 | createClub() | Request(correo="club@test.com") [Duplicado] | Excepción | IllegalStateException
     * 4 | createClub() | Request(tel="900900900") [Duplicado] | Excepción | IllegalStateException
     * 5 | createClub() | Request(pass="PlainPwd1!") | Club creado | verify encode
     * 6 | createClub() | Request(valido) [Rol no existe] | Excepción | RuntimeException
     * 7 | createClub() | Request(tel=null) | Club creado | no verifica existsByTelefono
     
     */

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClubServiceImpl clubService;

    private Club club;

    @BeforeEach
    void setUp() {
        club = new Club();
        club.setNombre("ClubTest");
        club.setCorreo("club@test.com");
        club.setTelefono("900900900");
        club.setContrasena("PlainPwd1!");
    }

    @Test
    void createClub_SuccessfulCreation() {
        // Descripción: Verifica que se cree un club correctamente cuando todos los datos son válidos.
        when(clubRepository.existsByNombre("ClubTest")).thenReturn(false);
        when(clubRepository.existsByCorreo("club@test.com")).thenReturn(false);
        when(clubRepository.existsByTelefono("900900900")).thenReturn(false);
        when(passwordEncoder.encode("PlainPwd1!")).thenReturn("encodedPwd");
        Rol rol = new Rol(); rol.setRol(RolNombre.ROLE_ADM_CLUB);
        when(rolRepository.findByRol(RolNombre.ROLE_ADM_CLUB)).thenReturn(Optional.of(rol));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Club saved = clubService.createClub(club);

        assertNotNull(saved);
        assertEquals("encodedPwd", saved.getContrasena());
        assertFalse(saved.getRoles().isEmpty());
        verify(clubRepository).save(any(Club.class));
        System.out.println("✓ Caso 1: Club válido - EXITOSO");
    }

    @Test
    void createClub_NameExists_Throws() {
        // Descripción: Verifica que se lance una excepción si el nombre del club ya existe.
        when(clubRepository.existsByNombre("ClubTest")).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> clubService.createClub(club));
        assertTrue(ex.getMessage().contains("nombre del club"));
        System.out.println("✓ Caso 2: Nombre duplicado - RECHAZADO");
    }

    @Test
    void createClub_CorreoExists_Throws() {
        // Descripción: Verifica que se lance una excepción si el correo electrónico ya está registrado.
        when(clubRepository.existsByNombre(anyString())).thenReturn(false);
        when(clubRepository.existsByCorreo("club@test.com")).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> clubService.createClub(club));
        assertTrue(ex.getMessage().contains("correo electrónico"));
        System.out.println("✓ Caso 3: Correo duplicado - RECHAZADO");
    }

    @Test
    void createClub_TelefonoExists_Throws() {
        // Descripción: Verifica que se lance una excepción si el teléfono ya está registrado.
        when(clubRepository.existsByNombre(anyString())).thenReturn(false);
        when(clubRepository.existsByCorreo(anyString())).thenReturn(false);
        when(clubRepository.existsByTelefono("900900900")).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> clubService.createClub(club));
        assertTrue(ex.getMessage().contains("teléfono"));
        System.out.println("✓ Caso 4: Teléfono duplicado - RECHAZADO");
    }

    @Test
    void createClub_PasswordEncoderCalled() {
        // Descripción: Verifica que la contraseña se encripte antes de guardarse.
        when(clubRepository.existsByNombre(anyString())).thenReturn(false);
        when(clubRepository.existsByCorreo(anyString())).thenReturn(false);
        when(clubRepository.existsByTelefono(anyString())).thenReturn(false);
        when(rolRepository.findByRol(RolNombre.ROLE_ADM_CLUB)).thenReturn(Optional.of(new Rol()));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        clubService.createClub(club);

        verify(passwordEncoder).encode("PlainPwd1!");
        System.out.println("✓ Caso 5: PasswordEncoder llamado - EXITOSO");
    }

    @Test
    void createClub_RoleMissing_Throws() {
        // Descripción: Verifica que se lance una excepción si el rol de administrador de club no existe.
        when(clubRepository.existsByNombre(anyString())).thenReturn(false);
        when(clubRepository.existsByCorreo(anyString())).thenReturn(false);
        when(clubRepository.existsByTelefono(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(rolRepository.findByRol(RolNombre.ROLE_ADM_CLUB)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> clubService.createClub(club));
        assertTrue(ex.getMessage().contains("Rol de club"));
        System.out.println("✓ Caso 6: Rol no encontrado - RECHAZADO");
    }

    @Test
    void createClub_PhoneNull_AllowsCreation() {
        // Descripción: Verifica que se permita crear un club sin teléfono (campo opcional o nulo).
        club.setTelefono(null);
        when(clubRepository.existsByNombre(anyString())).thenReturn(false);
        when(clubRepository.existsByCorreo(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(rolRepository.findByRol(RolNombre.ROLE_ADM_CLUB)).thenReturn(Optional.of(new Rol()));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Club saved = clubService.createClub(club);
        assertNotNull(saved);
        verify(clubRepository).save(any(Club.class));
        System.out.println("✓ Caso 7: Teléfono null permitido - EXITOSO");
    }

   
}
