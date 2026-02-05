package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import com.example.demo.util.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public String login(@org.springframework.lang.NonNull LoginRequest loginRequest) {
        try {
            // 1. Usamos el AuthenticationManager para validar las credenciales.
            // Esto ejecutará UserDetailsServiceImpl y todas las validaciones de Spring Security (incluyendo isEnabled()).
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getCorreoElectronico(), loginRequest.getContrasena())
            );

            // 2. Si la autenticación es exitosa, generamos el token JWT.
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtUtil.generateToken(userDetails.getUsername(), userDetails.getAuthorities());

        } catch (DisabledException e) {
            // Captura el error específico cuando isEnabled() devuelve false.
            throw new RuntimeException("La cuenta está deshabilitada. Por favor, contacte al administrador.");
        } catch (BadCredentialsException e) {
            // Captura el error de credenciales incorrectas (usuario no encontrado o contraseña errónea).
            throw new RuntimeException("Credenciales inválidas. Verifique su correo y contraseña.");
        } catch (AuthenticationException e) {
            // Captura cualquier otro error de autenticación no manejado.
            throw new RuntimeException("Error de autenticación: " + e.getMessage());
        }
    }
}