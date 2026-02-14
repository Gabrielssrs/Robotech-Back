package com.example.demo.service;

import com.example.demo.model.ConfiguracionSistema;
import com.example.demo.repository.ConfiguracionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private final ConfiguracionRepository repository;

    @PostConstruct
    public void init() {
        // 1. Prioridad: Variable de entorno FRONTEND_URL (Para Producción/Render)
        String frontendUrlEnv = System.getenv("FRONTEND_URL");
        if (frontendUrlEnv != null && !frontendUrlEnv.isBlank()) {
            repository.findByClave("BASE_URL").ifPresentOrElse(
                config -> {
                    if (!frontendUrlEnv.equals(config.getValor())) {
                        config.setValor(frontendUrlEnv);
                        repository.save(config);
                    }
                },
                () -> repository.save(new ConfiguracionSistema("BASE_URL", frontendUrlEnv, "URL base de la aplicación para enlaces en correos"))
            );
        } else {
            // 2. Fallback: Lógica para desarrollo local
            repository.findByClave("BASE_URL").ifPresent(config -> {
                if ("http://localhost:8080".equals(config.getValor())) {
                    config.setValor("http://127.0.0.1:5501");
                    repository.save(config);
                }
            });
            crearSiNoExiste("BASE_URL", "http://127.0.0.1:5501", "URL base de la aplicación para enlaces en correos");
        }

        // --- NUEVO: Configuración de BACKEND_URL (Para enlaces que apuntan a la API) ---
        
        // 1. Prioridad: Variable de entorno explícita BACKEND_URL (Manual)
        String backendUrlEnv = System.getenv("BACKEND_URL");
        
        // 2. Fallback: Variable automática de Render (RENDER_EXTERNAL_URL)
        if (backendUrlEnv == null || backendUrlEnv.isBlank()) {
            backendUrlEnv = System.getenv("RENDER_EXTERNAL_URL");
        }

        if (backendUrlEnv != null && !backendUrlEnv.isBlank()) {
            String finalUrl = backendUrlEnv;
            repository.findByClave("BACKEND_URL").ifPresentOrElse(
                config -> {
                    if (!finalUrl.equals(config.getValor())) {
                        config.setValor(finalUrl);
                        repository.save(config);
                    }
                },
                () -> repository.save(new ConfiguracionSistema("BACKEND_URL", finalUrl, "URL base del Backend (API)"))
            );
        } else {
            crearSiNoExiste("BACKEND_URL", "http://localhost:8080", "URL base del Backend (API)");
        }

        // --- NUEVO: Sincronizar credenciales de correo desde Variables de Entorno (Render) ---
        updateConfigFromEnv("SPRING_MAIL_HOST", "SMTP_HOST");
        updateConfigFromEnv("SPRING_MAIL_PORT", "SMTP_PORT");
        updateConfigFromEnv("SPRING_MAIL_USERNAME", "SMTP_USERNAME");
        updateConfigFromEnv("SPRING_MAIL_PASSWORD", "SMTP_PASSWORD");
        updateConfigFromEnv("APP_ROBOTECH_EMAIL", "EMAIL_OFICIAL_ROBOTECH");

        // Inicializar valores por defecto si no existen en la base de datos
        crearSiNoExiste("EMAIL_OFICIAL_ROBOTECH", "corpsrobotech@gmail.com", "Correo para recibir solicitudes de seguridad");
        crearSiNoExiste("TIEMPO_EDICION_MINUTOS", "15", "Tiempo en minutos habilitado para editar al Admin Principal");
        crearSiNoExiste("EXPIRACION_TOKEN_MINUTOS", "30", "Tiempo de expiración del token de solicitud por correo");
        crearSiNoExiste("EMAIL_SOPORTE", "corpsrobotech@gmail.com", "Correo de contacto para soporte técnico y ayuda a usuarios");
        crearSiNoExiste("TELEFONO_SOPORTE", "+51 900 000 000", "Número de teléfono oficial de atención al cliente");
        
        // Configuración SMTP (Correo Saliente)
        crearSiNoExiste("SMTP_HOST", "smtp.googlemail.com", "Servidor SMTP para envío de correos");
        crearSiNoExiste("SMTP_PORT", "465", "Puerto del servidor SMTP");
        crearSiNoExiste("SMTP_USERNAME", "soporterobotechti@gmail.com", "Usuario/Correo para autenticación SMTP");
        crearSiNoExiste("SMTP_PASSWORD", "", "Contraseña o App Password para SMTP");
        crearSiNoExiste("SMTP_AUTH", "true", "Habilitar autenticación SMTP (true/false)");
        crearSiNoExiste("SMTP_STARTTLS", "true", "Habilitar STARTTLS (true/false)");
    }

    private void updateConfigFromEnv(String envVar, String dbKey) {
        String value = System.getenv(envVar);
        if (value != null && !value.isBlank()) {
            repository.findByClave(dbKey).ifPresentOrElse(
                config -> {
                    if (!value.equals(config.getValor())) {
                        config.setValor(value);
                        repository.save(config);
                    }
                },
                () -> repository.save(new ConfiguracionSistema(dbKey, value, "Sincronizado desde variable de entorno"))
            );
        }
    }

    private void crearSiNoExiste(String clave, String valor, String descripcion) {
        if (repository.findByClave(clave).isEmpty()) {
            repository.save(new ConfiguracionSistema(clave, valor, descripcion));
        }
    }

    @Override
    public String getValor(String clave, String valorPorDefecto) {
        return repository.findByClave(clave)
                .map(ConfiguracionSistema::getValor)
                .orElse(valorPorDefecto);
    }

    @Override
    public int getValorInt(String clave, int valorPorDefecto) {
        return repository.findByClave(clave)
                .map(c -> {
                    try {
                        return Integer.parseInt(c.getValor());
                    } catch (NumberFormatException e) {
                        return valorPorDefecto;
                    }
                })
                .orElse(valorPorDefecto);
    }

    @Override
    @Transactional
    public void updateValor(String clave, String nuevoValor) {
        ConfiguracionSistema config = repository.findByClave(clave)
                .orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada: " + clave));
        config.setValor(nuevoValor);
        repository.save(config);
    }

    @Override
    public List<ConfiguracionSistema> getAll() {
        return repository.findAll();
    }
}