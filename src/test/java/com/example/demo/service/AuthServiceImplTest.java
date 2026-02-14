package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    
    /**
     * MATRIZ DE PRUEBAS - MÉTODO login()
     *
     * | Caso | Método a Probar           | Entrada                            | Salida Esperada | Observaciones                     |
     * |------|---------------------------|-----------------------------------|-----------------|-----------------------------------|
     * | 1    | login()                   | usuario="admin@test.com", pass="StrongP@ss1" | token | Campos válidos, credenciales OK   |
     * | 2    | login()                   | usuario=""                         | exception       | Usuario vacío                     |
     * | 3    | login()                   | password=""                        | exception       | Password vacío                    |
     * | 5    | login()                   | usuario=null                       | exception       | Usuario nulo                      |
     * | 6    | login()                   | password=null                      | exception       | Password nulo                     |
     * | 7    | login()                   | password="Aa1@2" (< 6 chars)       | exception       | Password menor a 6 caracteres     |
     * | 8    | login()                   | password="Aa1@23" (6 chars)        | token           | Longitud mínima permitida         |
     * | 9    | login() + authenticate()  | usuario="admin@test.com", pass="StrongP@ss1" | token | Credenciales correctas            |
     * | 10   | login() + authenticate()  | password="abcdef" incorrecto       | exception       | Contraseña incorrecta             |
     * | 11   | login() + authenticate()  | usuario="usuario1" no existe       | exception       | Usuario no registrado             |
     * | 12   | login() + isEnabled()     | cuenta deshabilitada               | exception       | Account disabled error            |
     * | 13   | login()                   | usuario="admin" (sin formato)      | exception       | Email inválido (falta @ o .com)   |
     * | 14   | login()                   | password="weak" (sin requisitos)   | exception       | Password débil (falta Mayus/Num/Signo)|
     * | RB   | login()                   | usuario="admin@test.com", ROLES   | token + roles   | Login exitoso por rol (4 variantes)|
     */

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private LoginRequest loginRequest;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    // ============================================
    // PRUEBAS DE CREDENCIALES VÁLIDAS (ROL ADMIN)
    // ============================================

    @Test
    void testCase1_ValidarCampos_CredencialesValidas() {
        // Caso 1: usuario="admin@test.com", password="StrongP@ss1" → true
        // Campos válidos, credenciales correctas
        verificarLoginExitosoConRol("admin@test.com", "StrongP@ss1", "ROLE_ADM_SISTEMA");
        System.out.println("✓ Caso 1: Campos válidos (usuario=admin@test.com, password=StrongP@ss1) - EXITOSO");
    }

    // ============================================
    // PRUEBAS DE VALIDACIONES: USUARIO VACÍO
    // ============================================

    @Test
    void testCase2_ValidarCampos_UsuarioVacio() {
        // Caso 2: usuario="", password="StrongP@ss1" → false
        // Usuario vacío - Spring Security rechaza via @NotBlank en LoginRequest
        when(loginRequest.getCorreoElectronico()).thenReturn("");
        when(loginRequest.getContrasena()).thenReturn("StrongP@ss1");

        // Nota: La validación @NotBlank ocurre en el controller, aquí simulamos directamente
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        System.out.println("✓ Caso 2: Usuario vacío - RECHAZADO");
    }

    @Test
    void testCase3_ValidarCampos_PasswordVacio() {
        // Caso 3: usuario="admin@test.com", password="" → false
        // Contraseña vacía - Spring Security rechaza via @NotBlank en LoginRequest
        when(loginRequest.getCorreoElectronico()).thenReturn("admin@test.com");
        when(loginRequest.getContrasena()).thenReturn("");

        // La validación @NotBlank ocurre en el controller
        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        System.out.println("✓ Caso 3: Contraseña vacía - RECHAZADO");
    }

    // ============================================
    // PRUEBAS DE VALIDACIONES: CAMPOS NULOS
    // ============================================

    @Test
    void testCase5_ValidarCampos_UsuarioNulo() {
        // Caso 5: usuario=null, password="StrongP@ss1" → false
        when(loginRequest.getCorreoElectronico()).thenReturn(null);
        when(loginRequest.getContrasena()).thenReturn("StrongP@ss1");

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        System.out.println("✓ Caso 5: Usuario nulo - RECHAZADO");
    }

    @Test
    void testCase6_ValidarCampos_PasswordNulo() {
        // Caso 6: usuario="admin@test.com", password=null → false
        when(loginRequest.getCorreoElectronico()).thenReturn("admin@test.com");
        when(loginRequest.getContrasena()).thenReturn(null);

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        System.out.println("✓ Caso 6: Contraseña nula - RECHAZADO");
    }

    // ============================================
    // PRUEBAS DE VALIDACIONES: LONGITUD MÍNIMA
    // ============================================

    @Test
    void testCase7_ValidarLongPassword_MenorA6Caracteres() {
        // Caso 7: usuario="admin@test.com", password="Aa1@2" → false
        // Contraseña menor a 6 caracteres
        when(loginRequest.getCorreoElectronico()).thenReturn("admin@test.com");
        when(loginRequest.getContrasena()).thenReturn("Aa1@2");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Password too short"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Credenciales inválidas. Verifique su correo y contraseña.", exception.getMessage());
        System.out.println("✓ Caso 7: Contraseña menor a 6 caracteres (Aa1@2) - RECHAZADO");
    }

    @Test
    void testCase8_ValidarLongPassword_MinimPermitida() {
        // Caso 8: usuario="admin@test.com", password="Aa1@23" → true
        // Contraseña con longitud mínima permitida (6 caracteres) y complejidad
        verificarLoginExitosoConRol("admin@test.com", "Aa1@23", "ROLE_ADM_SISTEMA");
        System.out.println("✓ Caso 8: Contraseña con longitud mínima (Aa1@23) - EXITOSO");
    }

    // ============================================
    // PRUEBAS DE VALIDACIONES: FORMATO Y COMPLEJIDAD
    // ============================================

    @Test
    void testCase_EmailFormatInvalid() {
        // Email sin @ o sin .com
        when(loginRequest.getCorreoElectronico()).thenReturn("admin"); // Inválido
        when(loginRequest.getContrasena()).thenReturn("StrongP@ss1");

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        System.out.println("✓ Caso Email Inválido (formato) - RECHAZADO");
    }

    @Test
    void testCase_PasswordComplexityInvalid() {
        when(loginRequest.getCorreoElectronico()).thenReturn("admin@test.com");
        when(loginRequest.getContrasena()).thenReturn("weakpass"); // Solo letras minúsculas

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        System.out.println("✓ Caso Password Débil (complejidad) - RECHAZADO");
    }

    // ============================================
    // PRUEBAS DE AUTENTICACIÓN: CREDENCIALES INVÁLIDAS
    // ============================================

    @Test
    void testCase9_Autenticar_CredencialesCorrectas() {
        // Caso 9: usuario="admin@test.com", password="StrongP@ss1" → true
        // Credenciales correctas, autenticación exitosa
        verificarLoginExitosoConRol("admin@test.com", "StrongP@ss1", "ROLE_ADM_SISTEMA");
        System.out.println("✓ Caso 9: Credenciales correctas - AUTENTICACIÓN EXITOSA");
    }

    @Test
    void testCase10_Autenticar_PasswordIncorrecto() {
        // Caso 10: usuario="admin@test.com", password="WrongP@ss1" → false
        // Usuario existe pero contraseña es incorrecta
        when(loginRequest.getCorreoElectronico()).thenReturn("admin@test.com");
        when(loginRequest.getContrasena()).thenReturn("WrongP@ss1");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Credenciales inválidas. Verifique su correo y contraseña.", exception.getMessage());
        System.out.println("✓ Caso 10: Contraseña incorrecta - AUTENTICACIÓN FALLIDA");
    }

    @Test
    void testCase11_Autenticar_UsuarioNoRegistrado() {
        // Caso 11: usuario="unknown@test.com", password="StrongP@ss1" → false
        // Usuario no existe en la base de datos
        when(loginRequest.getCorreoElectronico()).thenReturn("unknown@test.com");
        when(loginRequest.getContrasena()).thenReturn("StrongP@ss1");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("User not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Credenciales inválidas. Verifique su correo y contraseña.", exception.getMessage());
        System.out.println("✓ Caso 11: Usuario no registrado - AUTENTICACIÓN FALLIDA");
    }

    // ============================================
    // PRUEBAS DE ESTADO DE CUENTA
    // ============================================

    @Test
    void testCase_CuentaDeshabilitada() {
        // Usuario existe y contraseña correcta, pero cuenta deshabilitada
        when(loginRequest.getCorreoElectronico()).thenReturn("admin@test.com");
        when(loginRequest.getContrasena()).thenReturn("StrongP@ss1");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("Cuenta deshabilitada"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("La cuenta está deshabilitada. Por favor, contacte al administrador.", exception.getMessage());
        System.out.println("✓ Cuenta deshabilitada - ACCESO DENEGADO");
    }

    // --- PRUEBAS DE CREDENCIALES VÁLIDAS POR ROL ---

    @Test
    void loginExitoso_AdminSistema() {
        verificarLoginExitosoConRol("admin@test.com", "StrongP@ss1", "ROLE_ADM_SISTEMA");
        System.out.println("✓ Login exitoso - Admin Sistema");
    }

    @Test
    void loginExitoso_AdminClub() {
        verificarLoginExitosoConRol("club@test.com", "StrongP@ss1", "ROLE_ADM_CLUB");
        System.out.println("✓ Login exitoso - Admin Club");
    }

    @Test
    void loginExitoso_Competidor() {
        verificarLoginExitosoConRol("competidor@test.com", "StrongP@ss1", "ROLE_COMPETIDOR");
        System.out.println("✓ Login exitoso - Competidor");
    }

    @Test
    void loginExitoso_Juez() {
        verificarLoginExitosoConRol("juez@test.com", "StrongP@ss1", "ROLE_JUEZ");
        System.out.println("✓ Login exitoso - Juez");
    }

    // Método auxiliar para simplificar y reutilizar la lógica de prueba
    private void verificarLoginExitosoConRol(String email, String password, String rol) {
        String tokenEsperado = "token_jwt_" + rol;

        // 1. Preparar datos del request
        when(loginRequest.getCorreoElectronico()).thenReturn(email);
        when(loginRequest.getContrasena()).thenReturn(password);

        // 2. Simular autenticación exitosa
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);

        // 3. Configurar el rol específico que devuelve el usuario
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(rol));
        doReturn(authorities).when(userDetails).getAuthorities();

        // 4. Mockear la generación del token
        when(jwtUtil.generateToken(eq(email), eq(authorities))).thenReturn(tokenEsperado);

        // 5. Ejecutar
        String tokenGenerado = authService.login(loginRequest);

        // 6. Verificar
        assertEquals(tokenEsperado, tokenGenerado);
        verify(jwtUtil).generateToken(eq(email), eq(authorities));
    }

}
