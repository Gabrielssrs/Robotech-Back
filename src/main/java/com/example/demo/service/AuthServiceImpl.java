package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import com.example.demo.util.JwtUtil;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ConfiguracionService configuracionService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // Repositorios de usuarios para buscar y actualizar contraseña
    private final AdministradorRepository administradorRepository;
    private final ClubRepository clubRepository;
    private final CompetidorRepository competidorRepository;
    private final JuezRepository juezRepository;

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

    @Override
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String email = request.getEmail();
        String dni = request.getDni();

        boolean userExists = false;
        boolean dataMatches = false;

        // 1. Verificar Administrador
        Optional<Administrador> admin = administradorRepository.findByCorreo(email);
        if (admin.isPresent()) {
            userExists = true;
            if (admin.get().getDni().equals(dni)) dataMatches = true;
        }

        // 2. Verificar Competidor (si no se encontró antes)
        if (!userExists) {
            Optional<Competidor> competidor = competidorRepository.findByCorreoElectronico(email);
            if (competidor.isPresent()) {
                userExists = true;
                if (competidor.get().getDni().equals(dni)) dataMatches = true;
            }
        }

        // 3. Verificar Juez (si no se encontró antes)
        if (!userExists) {
            Optional<Juez> juez = juezRepository.findByCorreo(email);
            if (juez.isPresent()) {
                userExists = true;
                if (juez.get().getDni().equals(dni)) dataMatches = true;
            }
        }

        // Nota: Los Clubs no tienen DNI en el sistema actual, por lo que no pueden recuperar contraseña por este método
        // o fallarán en la validación de datos si se intenta con su correo.

        // Validaciones y Errores
        if (!userExists) {
            throw new IllegalArgumentException("El usuario no existe.");
        }

        if (!dataMatches) {
            throw new IllegalArgumentException("Los datos proporcionados no coinciden con el usuario.");
        }

        // Generar token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1)); // Expira en 1 hora
        passwordResetTokenRepository.save(resetToken);

        // Obtener configuración
        String baseUrl = configuracionService.getValor("BASE_URL", "http://127.0.0.1:5501");
        String emailSoporte = configuracionService.getValor("EMAIL_SOPORTE", "corpsrobotech@gmail.comspring.mail.username=ramirezsolongabriel91@gmail.com");

        // Construir enlace y mensaje
        String link = baseUrl + "/restablecer_password.html?token=" + token;
        String mensaje = String.format(
            "Hola,\n\nSe ha recibido una solicitud para restablecer la contraseña de su cuenta en Robotech.\n\n" +
            "Si fue usted, continúe con los pasos y acepte en el siguiente enlace:\n%s\n\n" +
            "Si no fue usted, por favor ignore este mensaje.\n\n" +
            "Atentamente,\nSoporte Robotech (%s)",
            link, emailSoporte
        );

        emailService.enviarCorreoSimple(email, "Recuperación de Contraseña - Robotech", mensaje);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o no encontrado."));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token ha expirado. Por favor solicite uno nuevo.");
        }

        String email = resetToken.getEmail();
        String newPasswordEncoded = passwordEncoder.encode(request.getNewPassword());
        boolean updated = false;

        // Buscar y actualizar en el repositorio correspondiente
        if (administradorRepository.findByCorreo(email).map(u -> { u.setContrasena(newPasswordEncoded); administradorRepository.save(u); return true; }).orElse(false)) {
            updated = true;
        } else if (clubRepository.findByCorreo(email).map(u -> { u.setContrasena(newPasswordEncoded); clubRepository.save(u); return true; }).orElse(false)) {
            updated = true;
        } else if (competidorRepository.findByCorreoElectronico(email).map(u -> { u.setContrasena(newPasswordEncoded); competidorRepository.save(u); return true; }).orElse(false)) {
            updated = true;
        } else if (juezRepository.findByCorreo(email).map(u -> { u.setContrasena(newPasswordEncoded); juezRepository.save(u); return true; }).orElse(false)) {
            updated = true;
        }

        if (updated) {
            passwordResetTokenRepository.delete(resetToken); // Consumir el token
        } else {
            throw new IllegalArgumentException("Error al actualizar la contraseña: Usuario no encontrado.");
        }
    }
}