package com.example.demo.service;

import com.example.demo.dto.JuezRequest;
import com.example.demo.dto.JuezResponse;
import com.example.demo.model.Categoria;
import com.example.demo.model.Juez;
import com.example.demo.model.Rol;
import com.example.demo.model.RolNombre;
import com.example.demo.model.Sede;
import com.example.demo.model.NivelCredencial;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.EncuentroRepository;
import com.example.demo.repository.JuezRepository;
import com.example.demo.repository.RolRepository;
import com.example.demo.repository.SedeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateJuezServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO createJuez()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | createJuez() | Request(nombre="Ana Judge", dni="87654321", tel="999888777", correo="ana@juez.com", pass="Aa1@abcd", nivel=JUNIOR, sede=100, cats=[10]) | Juez creado | Asigna rol, sede y especialidades
     * 2 | createJuez() | Request(correo="exists@juez.com") | Excepción | IllegalArgumentException
     * 3 | createJuez() | Request(dni="11112222") | Excepción | IllegalArgumentException
     * 4 | createJuez() | Request(telefono="955500000") | Excepción | IllegalArgumentException
     * 5 | createJuez() | Request(contrasena=null) | Excepción | IllegalArgumentException
     * 6 | createJuez() | Request(nivelCredencial=null) | Excepción | IllegalArgumentException
     * 7 | createJuez() | Request(sedeId=999) | Excepción | IllegalArgumentException
     * 8 | createJuez() | Request(categoriaIds=[10, 999]) | Excepción | IllegalArgumentException
     * 9 | createJuez() | Request(categoriaIds=null) | Juez creado | No asigna especialidades
     * 10 | createJuez() | Request(categoriaIds=[]) | Juez creado | No asigna especialidades
     * 11 | createJuez() | Request(valido) [Rol no encontrado] | Excepción | IllegalStateException
     * 12 | createJuez() | Request(valido) | Juez creado | PasswordEncoder.encode llamado
     * 13 | createJuez() | Request(contrasena="weak") | Excepción | IllegalArgumentException
     * 14 | createJuez() | Request(correo="correo-invalido") | Excepción | IllegalArgumentException
     * 15 | createJuez() | Request(telefono="123456" / "812345678") | Excepción | IllegalArgumentException
     * 16 | createJuez() | Request(dni="1234567") | Excepción | IllegalArgumentException
     */

    @Mock
    private JuezRepository juezRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private SedeRepository sedeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EncuentroRepository encuentroRepository;

    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private JuezServiceImpl juezService;

    private Categoria cat1;
    private Sede sede;

    @BeforeEach
    void setUp() {
        cat1 = new Categoria();
        cat1.setId(10L);
        cat1.setNombre("Combate Ligero");

        sede = new Sede();
        sede.setId(100L);
        sede.setNombre("Sede Central");
    }

    @Test
    void createJuez_SuccessfulCreation() {
        // Descripción: Verifica que se pueda crear un juez correctamente cuando todos los datos son válidos.
        JuezRequest req = new JuezRequest();
        req.setNombre("Ana Judge");
        req.setDni("87654321");
        req.setTelefono("999888777");
        req.setCorreo("ana@juez.com");
        req.setContrasena("Aa1@abcd");
        req.setNivelCredencial(NivelCredencial.NIVEL_1_JUNIOR);
        req.setSedeId(100L);
        req.setCategoriaIds(Set.of(10L));

        when(juezRepository.existsByCorreo("ana@juez.com")).thenReturn(false);
        when(juezRepository.existsByDni("87654321")).thenReturn(false);
        when(juezRepository.existsByTelefono("999888777")).thenReturn(false);
        when(passwordEncoder.encode("Aa1@abcd")).thenReturn("encodedPwd");
        Rol rol = new Rol(); rol.setRol(RolNombre.ROLE_JUEZ);
        when(rolRepository.findByRol(RolNombre.ROLE_JUEZ)).thenReturn(Optional.of(rol));
        when(categoriaRepository.findAllById(Set.of(10L))).thenReturn(List.of(cat1));
        when(sedeRepository.findById(100L)).thenReturn(Optional.of(sede));
        when(juezRepository.save(any(Juez.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JuezResponse resp = juezService.createJuez(req);

        assertNotNull(resp);
        assertEquals("ana@juez.com", resp.getCorreo());
        verify(juezRepository).save(any(Juez.class));
        verify(passwordEncoder).encode("Aa1@abcd");
        System.out.println("✓ Caso 1: Creación exitosa con todos los datos - EXITOSO");
    }

    @Test
    void createJuez_EmailAlreadyExists_Throws() {
        // Descripción: Verifica que se lance una excepción si el correo electrónico ya está registrado.
        JuezRequest req = new JuezRequest();
        req.setCorreo("exists@juez.com");
        when(juezRepository.existsByCorreo("exists@juez.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().contains("correo electrónico"));
        System.out.println("✓ Caso 2: Correo ya existente - RECHAZADO");
    }

    @Test
    void createJuez_DniAlreadyExists_Throws() {
        // Descripción: Verifica que se lance una excepción si el DNI ya está registrado.
        JuezRequest req = new JuezRequest();
        req.setCorreo("test@test.com");
        req.setDni("11112222");
        when(juezRepository.existsByCorreo(anyString())).thenReturn(false);
        when(juezRepository.existsByDni("11112222")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().contains("DNI"));
        System.out.println("✓ Caso 3: DNI ya existente - RECHAZADO");
    }

    @Test
    void createJuez_TelefonoAlreadyExists_Throws() {
        // Descripción: Verifica que se lance una excepción si el teléfono ya está registrado.
        JuezRequest req = new JuezRequest();
        req.setCorreo("test@test.com");
        req.setDni("12345678");
        req.setTelefono("955500000");
        when(juezRepository.existsByCorreo(anyString())).thenReturn(false);
        when(juezRepository.existsByDni(anyString())).thenReturn(false);
        when(juezRepository.existsByTelefono("955500000")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().contains("teléfono"));
        System.out.println("✓ Caso 4: Teléfono ya existente - RECHAZADO");
    }

    @Test
    void createJuez_MissingPassword_Throws() {
        // Descripción: Verifica que se lance una excepción si la contraseña es nula o vacía.
        JuezRequest req = new JuezRequest();
        req.setCorreo("test@test.com");
        req.setDni("12345678");
        req.setTelefono("955500000");
        req.setContrasena(null);
        when(juezRepository.existsByCorreo(anyString())).thenReturn(false);
        when(juezRepository.existsByDni(anyString())).thenReturn(false);
        when(juezRepository.existsByTelefono(anyString())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().contains("contraseña"));
        System.out.println("✓ Caso 5: Contraseña faltante - RECHAZADO");
    }

    @Test
    void createJuez_MissingNivelCredencial_Throws() {
        // Descripción: Verifica que se lance una excepción si el nivel de credencial no se proporciona.
        JuezRequest req = new JuezRequest();
        req.setCorreo("test@test.com");
        req.setDni("12345678");
        req.setTelefono("955500000");
        req.setContrasena("Aa1@abcd");
        when(juezRepository.existsByCorreo(anyString())).thenReturn(false);
        when(juezRepository.existsByDni(anyString())).thenReturn(false);
        when(juezRepository.existsByTelefono(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().contains("nivel de credencial"));
        System.out.println("✓ Caso 6: Nivel de credencial faltante - RECHAZADO");
    }

    @Test
    void createJuez_InvalidSedeId_Throws() {
        // Descripción: Verifica que se lance una excepción si el ID de la sede proporcionado no existe.
        JuezRequest req = new JuezRequest();
        req.setCorreo("test@test.com");
        req.setDni("12345678");
        req.setTelefono("955500000");
        req.setContrasena("Aa1@abcd");
        req.setNivelCredencial(NivelCredencial.NIVEL_1_JUNIOR);
        req.setSedeId(999L);
        when(juezRepository.existsByCorreo(anyString())).thenReturn(false);
        when(juezRepository.existsByDni(anyString())).thenReturn(false);
        when(juezRepository.existsByTelefono(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(sedeRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().contains("Sede no encontrada"));
        System.out.println("✓ Caso 7: Sede inválida - RECHAZADO");
    }

    @Test
    void createJuez_InvalidCategoriaId_Throws() {
        // Descripción: Verifica que se lance una excepción si alguno de los IDs de categoría no existe.
        JuezRequest req = new JuezRequest();
        req.setCorreo("test@test.com");
        req.setDni("12345678");
        req.setTelefono("955500000");
        req.setContrasena("Aa1@abcd");
        req.setNivelCredencial(NivelCredencial.NIVEL_1_JUNIOR);
        req.setCategoriaIds(Set.of(10L, 999L));
        when(juezRepository.existsByCorreo(anyString())).thenReturn(false);
        when(juezRepository.existsByDni(anyString())).thenReturn(false);
        when(juezRepository.existsByTelefono(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(categoriaRepository.findAllById(Set.of(10L, 999L))).thenReturn(List.of(cat1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().contains("IDs de categoría"));
        System.out.println("✓ Caso 8: ID de categoría inválida - RECHAZADO");
    }

    @Test
    void createJuez_NullOrEmptyCategoriaIds_AllowsCreation() {
        // Descripción: Verifica que se permita crear un juez sin asignar categorías (lista nula o vacía).
        JuezRequest req = new JuezRequest();
        req.setNombre("SinCats");
        req.setDni("22223333");
        req.setCorreo("nocats@juez.com");
        req.setTelefono("955599999");
        req.setContrasena("Aa1@abcd");
        req.setNivelCredencial(NivelCredencial.NIVEL_1_JUNIOR);
        req.setCategoriaIds(null); // allowed

        when(juezRepository.existsByCorreo(anyString())).thenReturn(false);
        when(juezRepository.existsByDni(anyString())).thenReturn(false);
        when(juezRepository.existsByTelefono(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        Rol rol = new Rol(); rol.setRol(RolNombre.ROLE_JUEZ);
        when(rolRepository.findByRol(RolNombre.ROLE_JUEZ)).thenReturn(Optional.of(rol));
        when(juezRepository.save(any(Juez.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JuezResponse resp = juezService.createJuez(req);
        assertNotNull(resp);
        assertEquals("nocats@juez.com", resp.getCorreo());
        System.out.println("✓ Caso 9: Categorías nulas (sin especialidad) - EXITOSO");
    }

    @Test
    void createJuez_RoleMissing_Throws() {
        // Descripción: Verifica que se lance una excepción crítica si el rol ROLE_JUEZ no está configurado en la BD.
        JuezRequest req = new JuezRequest();
        req.setCorreo("test@test.com");
        req.setDni("12345678");
        req.setTelefono("955500000");
        req.setContrasena("Aa1@abcd");
        req.setNivelCredencial(NivelCredencial.NIVEL_1_JUNIOR);
        when(juezRepository.existsByCorreo(anyString())).thenReturn(false);
        when(juezRepository.existsByDni(anyString())).thenReturn(false);
        when(juezRepository.existsByTelefono(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(rolRepository.findByRol(RolNombre.ROLE_JUEZ)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().contains("ROLE_JUEZ"));
        System.out.println("✓ Caso 11: Rol no configurado - RECHAZADO");
    }

    @Test
    void createJuez_PasswordEncoderCalled() {
        // Descripción: Verifica que la contraseña se encripte antes de guardarse.
        JuezRequest req = new JuezRequest();
        req.setCorreo("test@test.com");
        req.setDni("12345678");
        req.setTelefono("955500000");
        req.setContrasena("Aa1@abcd");
        req.setNivelCredencial(NivelCredencial.NIVEL_1_JUNIOR);
        when(juezRepository.existsByCorreo(anyString())).thenReturn(false);
        when(juezRepository.existsByDni(anyString())).thenReturn(false);
        when(juezRepository.existsByTelefono(anyString())).thenReturn(false);
        when(rolRepository.findByRol(RolNombre.ROLE_JUEZ)).thenReturn(Optional.of(new Rol()));
        when(juezRepository.save(any(Juez.class))).thenAnswer(invocation -> invocation.getArgument(0));

        juezService.createJuez(req);

        verify(passwordEncoder).encode("Aa1@abcd");
        System.out.println("✓ Caso 12: Verificación de encriptación de contraseña - EXITOSO");
    }

    @Test
    void createJuez_InvalidPasswordFormat_Throws() {
        // Descripción: Verifica que se lance excepción si la contraseña no cumple con los requisitos de complejidad.
        JuezRequest req = new JuezRequest();
        req.setNombre("Juez Test");
        req.setDni("12345678");
        req.setTelefono("912345678");
        req.setCorreo("juez@test.com");
        req.setContrasena("weak"); // No cumple requisitos
        req.setNivelCredencial(NivelCredencial.NIVEL_1_JUNIOR);
        req.setSedeId(100L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().toLowerCase().contains("contraseña"));
        System.out.println("✓ Caso 13: Contraseña formato inválido - RECHAZADO");
    }

    @Test
    void createJuez_InvalidEmailFormat_Throws() {
        // Descripción: Verifica que se lance excepción si el correo no tiene un formato válido.
        JuezRequest req = new JuezRequest();
        req.setNombre("Juez Test");
        req.setDni("12345678");
        req.setTelefono("912345678");
        req.setCorreo("correo-invalido"); // Sin @ ni dominio
        req.setContrasena("Aa1@abcd");
        req.setNivelCredencial(NivelCredencial.NIVEL_1_JUNIOR);
        req.setSedeId(100L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().toLowerCase().contains("correo"));
        System.out.println("✓ Caso 14: Correo formato inválido - RECHAZADO");
    }

    @Test
    void createJuez_InvalidPhoneFormat_Throws() {
        // Descripción: Verifica que se lance excepción si el teléfono no tiene 9 dígitos o no empieza con 9.
        JuezRequest req = new JuezRequest();
        req.setNombre("Juez Test");
        req.setDni("12345678");
        req.setCorreo("juez@test.com");
        req.setContrasena("Aa1@abcd");
        req.setNivelCredencial(NivelCredencial.NIVEL_1_JUNIOR);
        req.setSedeId(100L);

        // Caso A: Longitud incorrecta
        req.setTelefono("123456");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().toLowerCase().contains("teléfono"));

        // Caso B: No empieza con 9
        req.setTelefono("812345678");
        ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().toLowerCase().contains("teléfono"));

        // Caso C: Contiene letras
        req.setTelefono("91234567a");
        ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().toLowerCase().contains("teléfono"));

        System.out.println("✓ Caso 15: Teléfono formato inválido - RECHAZADO");
    }

    @Test
    void createJuez_InvalidDniFormat_Throws() {
        // Descripción: Verifica que se lance excepción si el DNI no tiene 8 dígitos.
        JuezRequest req = new JuezRequest();
        req.setNombre("Juez Test");
        req.setTelefono("912345678");
        req.setCorreo("juez@test.com");
        req.setContrasena("Aa1@abcd");
        req.setNivelCredencial(NivelCredencial.NIVEL_1_JUNIOR);
        req.setDni("1234567"); // 7 dígitos
        req.setSedeId(100L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.createJuez(req));
        assertTrue(ex.getMessage().toLowerCase().contains("dni"));
        System.out.println("✓ Caso 16: DNI formato inválido - RECHAZADO");
    }
}
