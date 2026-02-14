package com.example.demo.service;

import com.example.demo.dto.AdminCreateRequest;
import com.example.demo.dto.AdminResponse;
import com.example.demo.dto.AdminUpdateRequest;
import com.example.demo.model.Administrador;
import com.example.demo.model.AdministradorEstado;
import com.example.demo.model.Rol;
import com.example.demo.model.RolNombre;
import com.example.demo.model.SolicitudEdicion;
import com.example.demo.repository.AdministradorRepository;
import com.example.demo.repository.RolRepository;
import com.example.demo.repository.SolicitudEdicionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemAdminServiceImpl implements SystemAdminService {

    private final AdministradorRepository administradorRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final SolicitudEdicionRepository solicitudEdicionRepository;
    private final EmailService emailService;
    private final ConfiguracionService configuracionService;

    @Override
    @Transactional
    public AdminResponse createAdmin(AdminCreateRequest request) {
        // Seguridad: Verificar que el usuario autenticado sea el Admin Principal (ID 1)
        String emailActual = SecurityContextHolder.getContext().getAuthentication().getName();
        Administrador adminActual = administradorRepository.findByCorreo(emailActual)
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado no encontrado."));

        if (adminActual.getId() != 1L) {
            throw new SecurityException("Acceso denegado: Solo el Administrador Principal puede crear nuevos administradores.");
        }

        // 1. Validar duplicados (Correo)
        if (administradorRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        // 2. Crear entidad
        Administrador admin = new Administrador();
        admin.setNombre(request.getNombre());
        admin.setDni(request.getDni());
        admin.setTelefono(request.getTelefono());
        admin.setCorreo(request.getCorreo());
        
        // Mapear el booleano del request al Enum
        boolean habilitado = request.getIsEnabled() == null || request.getIsEnabled();
        admin.setEstado(habilitado ? AdministradorEstado.ACTIVO : AdministradorEstado.SUSPENDIDO);

        // 3. Encriptar contraseña
        admin.setContrasena(passwordEncoder.encode(request.getContrasena()));

        // 4. Asignar Rol de Admin de Sistema
        Rol rolAdmin = rolRepository.findByRol(RolNombre.ROLE_ADM_SISTEMA)
                .orElseThrow(() -> new IllegalStateException("El rol ROLE_ADM_SISTEMA no está configurado en la base de datos."));
        admin.setRoles(Set.of(rolAdmin));

        // 5. Guardar
        Administrador savedAdmin = administradorRepository.save(admin);

        // 6. Mapear a Response
        return mapToResponse(savedAdmin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResponse> getAllAdmins() {
        return administradorRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AdminResponse updateAdmin(Long id, AdminUpdateRequest request) {
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado con ID: " + id));

        // Detectar si hay cambios en datos sensibles
        boolean isSensitiveChange = false;
        if (request.getDni() != null && !request.getDni().equals(admin.getDni())) isSensitiveChange = true;
        if (request.getCorreo() != null && !request.getCorreo().equals(admin.getCorreo())) isSensitiveChange = true;
        if (request.getContrasena() != null && !request.getContrasena().isBlank()) isSensitiveChange = true;

        // Lógica especial para el Admin Principal (ID 1)
        if (id == 1L && isSensitiveChange) {
            int minutos = configuracionService.getValorInt("TIEMPO_EDICION_MINUTOS", 15);
            boolean habilitado = solicitudEdicionRepository.isEdicionHabilitada(id, LocalDateTime.now().minusMinutes(minutos));
            if (!habilitado) {
                throw new SecurityException("La edición de datos sensibles (DNI, Correo, Contraseña) del Administrador Principal está bloqueada. Debe solicitar desbloqueo primero.");
            }
        }

        // Actualizar campos
        if (request.getNombre() != null) admin.setNombre(request.getNombre());
        if (request.getDni() != null) admin.setDni(request.getDni());
        if (request.getTelefono() != null) admin.setTelefono(request.getTelefono());
        
        // Actualizar Correo (con validación de unicidad)
        if (request.getCorreo() != null && !request.getCorreo().equals(admin.getCorreo())) {
            if (administradorRepository.findByCorreo(request.getCorreo()).isPresent()) {
                throw new IllegalArgumentException("El correo electrónico ya está registrado por otro administrador.");
            }
            admin.setCorreo(request.getCorreo());
        }

        // Actualizar Contraseña (encriptada)
        if (request.getContrasena() != null && !request.getContrasena().isBlank()) {
            admin.setContrasena(passwordEncoder.encode(request.getContrasena()));
        }

        Administrador updatedAdmin = administradorRepository.save(admin);
        return mapToResponse(updatedAdmin);
    }

    @Override
    @Transactional
    public void solicitarDesbloqueoEdicion(Long id) {
        if (id != 1L) {
            throw new IllegalArgumentException("Esta función es exclusiva para el Administrador Principal (ID 1).");
        }

        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrador principal no encontrado."));

        // Generar token
        String token = UUID.randomUUID().toString();

        SolicitudEdicion solicitud = new SolicitudEdicion();
        solicitud.setAdministrador(admin);
        solicitud.setToken(token);
        solicitud.setAceptada(false);
        solicitud.setFechaCreacion(LocalDateTime.now());
        
        solicitudEdicionRepository.save(solicitud);

        // Enviar correo
        String baseUrl = configuracionService.getValor("BASE_URL", "http://localhost:8080");
        String link = baseUrl + "/api/v1/admins/desbloquear?token=" + token;
        
        int minutos = configuracionService.getValorInt("TIEMPO_EDICION_MINUTOS", 15);
        
        String mensaje = String.format(
            "Hola,\n\nSe ha recibido una solicitud para editar los datos del Administrador Principal (ID 1).\n" +
            "Si usted realizó esta solicitud, haga clic en el siguiente enlace para habilitar la edición por %d minutos:\n\n" +
            "%s\n\n" +
            "Si no fue usted, ignore este mensaje.",
            minutos,
            link
        );

        // Se envía al correo oficial de Robotech obtenido de la configuración dinámica
        String emailOficial = configuracionService.getValor("EMAIL_OFICIAL_ROBOTECH", "oficial@robotech.com");
        emailService.enviarCorreoSimple(emailOficial, "Solicitud de Edición - Admin Principal", mensaje);
    }

    @Override
    @Transactional
    public void aceptarDesbloqueoEdicion(String token) {
        SolicitudEdicion solicitud = solicitudEdicionRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado."));

        if (solicitud.isAceptada()) {
            throw new IllegalStateException("Esta solicitud ya fue aceptada.");
        }

        // Validar expiración del token (ej. 30 minutos para hacer click en el correo)
        int expiracionMinutos = configuracionService.getValorInt("EXPIRACION_TOKEN_MINUTOS", 30);
        if (solicitud.getFechaCreacion().plusMinutes(expiracionMinutos).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El enlace ha expirado.");
        }

        solicitud.setAceptada(true);
        solicitud.setFechaAceptacion(LocalDateTime.now());
        solicitudEdicionRepository.save(solicitud);
    }

    @Override
    @Transactional
    public void suspenderAdmin(Long id) {
        if (id == 1L) {
            throw new IllegalArgumentException("No se puede suspender al Administrador Principal del sistema.");
        }
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado con ID: " + id));
        
        admin.setEstado(AdministradorEstado.SUSPENDIDO);
        administradorRepository.save(admin);
    }

    @Override
    @Transactional
    public void activarAdmin(Long id) {
        Administrador admin = administradorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado con ID: " + id));
        
        admin.setEstado(AdministradorEstado.ACTIVO);
        administradorRepository.save(admin);
    }

    private AdminResponse mapToResponse(Administrador admin) {
        AdminResponse response = new AdminResponse();
        response.setId(admin.getId());
        response.setNombre(admin.getNombre());
        response.setDni(admin.getDni());
        response.setTelefono(admin.getTelefono());
        response.setCorreo(admin.getCorreo());
        response.setEstado(admin.getEstado() != null ? admin.getEstado().name() : "ACTIVO");
        
        if (admin.getRoles() != null && !admin.getRoles().isEmpty()) {
            response.setRol(admin.getRoles().iterator().next().getRol().name());
        }
        
        return response;
    }
}