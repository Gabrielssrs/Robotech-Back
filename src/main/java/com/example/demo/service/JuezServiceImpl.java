package com.example.demo.service;

import com.example.demo.dto.EncuentroResponse;
import com.example.demo.dto.JuezRequest;
import com.example.demo.dto.JuezResponse;
import com.example.demo.dto.JuezProfileUpdateRequest;
import com.example.demo.dto.JuezUpdateRequest;
import com.example.demo.model.JuezEstado;
import com.example.demo.model.Encuentro;
import com.example.demo.model.Categoria;
import com.example.demo.model.Juez;
import com.example.demo.model.Sede;
import com.example.demo.model.Rol;
import com.example.demo.model.RolNombre;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.JuezRepository;
import com.example.demo.repository.EncuentroRepository;
import com.example.demo.repository.SedeRepository;
import com.example.demo.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JuezServiceImpl implements JuezService {

    private final JuezRepository juezRepository;
    private final RolRepository rolRepository;
    private final CategoriaRepository categoriaRepository; // Añadido para buscar categorías
    private final SedeRepository sedeRepository;
    private final EncuentroRepository encuentroRepository; // Inyectar repositorio de encuentros
    private final PasswordEncoder passwordEncoder;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional
    public JuezResponse createJuez(JuezRequest request) {
        if (juezRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("El correo electrónico ya está en uso.");
        }
        if (juezRepository.existsByDni(request.getDni())) {
            throw new IllegalArgumentException("El DNI ya está registrado.");
        }
        if (juezRepository.existsByTelefono(request.getTelefono())) {
            throw new IllegalArgumentException("El teléfono ya está registrado.");
        }
        if (request.getContrasena() == null || request.getContrasena().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria para crear un nuevo juez.");
        }

        Juez juez = new Juez();
        juez.setNombre(request.getNombre());
        juez.setDni(request.getDni());
        juez.setTelefono(request.getTelefono());
        juez.setCorreo(request.getCorreo());
        juez.setContrasena(passwordEncoder.encode(request.getContrasena()));
        
        // Asignar el nivel de credencial (con un valor por defecto si no se proporciona)
        if (request.getNivelCredencial() == null) {
            throw new IllegalArgumentException("El nivel de credencial es obligatorio.");
        }
        juez.setNivelCredencial(request.getNivelCredencial());
        juez.setEstado(JuezEstado.EN_CAPACITACION); // Estado por defecto para un nuevo juez

        // Asignar Sede
        if (request.getSedeId() != null) {
            Sede sede = sedeRepository.findById(request.getSedeId())
                    .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada con ID: " + request.getSedeId()));
            juez.setSede(sede);
        }

        // Asignar especialidades (categorías) si se proporcionan
        if (request.getCategoriaIds() != null && !request.getCategoriaIds().isEmpty()) {
            Set<Categoria> especialidades = new HashSet<>(categoriaRepository.findAllById(request.getCategoriaIds()));
            if (especialidades.size() != request.getCategoriaIds().size()) {
                throw new IllegalArgumentException("Una o más IDs de categoría no son válidas.");
            }
            juez.setEspecialidades(especialidades);
        }

        Rol juezRol = rolRepository.findByRol(RolNombre.ROLE_JUEZ)
                .orElseThrow(() -> new IllegalStateException("El rol ROLE_JUEZ no se encuentra en la base de datos."));
        juez.setRoles(Set.of(juezRol));

        Juez savedJuez = juezRepository.save(juez);
        return mapToJuezResponse(savedJuez);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JuezResponse> getAllJueces() {
        List<Juez> jueces = juezRepository.findAll(); // Primero obtenemos todos los jueces
        // Luego, mapeamos cada uno. La transacción sigue abierta aquí.
        return jueces.stream()
                .map(this::mapToJuezResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JuezResponse getJuezById(@NonNull Long id) {
        return juezRepository.findById(id)
                .map(this::mapToJuezResponse)
                .orElseThrow(() -> new IllegalArgumentException("Juez no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public JuezResponse updateJuez(@NonNull Long id, JuezUpdateRequest request) {
        Juez juez = juezRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Juez no encontrado con id: " + id));

        // Actualizar estado si se proporciona en la solicitud
        if (request.getEstado() != null && !request.getEstado().isBlank()) {
            // Convertir el String del request al Enum correspondiente
            JuezEstado nuevoEstado = JuezEstado.valueOf(request.getEstado().toUpperCase());
            juez.setEstado(nuevoEstado);
        }

        if (request.getCategoriaIds() != null) {
            Set<Categoria> nuevasEspecialidades = new HashSet<>(categoriaRepository.findAllById(request.getCategoriaIds()));
            if (nuevasEspecialidades.size() != request.getCategoriaIds().size()) {
                throw new IllegalArgumentException("Una o más IDs de categoría no son válidas.");
            }
            // Asignar directamente el nuevo conjunto de especialidades
            juez.setEspecialidades(nuevasEspecialidades);
        } else {
            // Si se envía una lista vacía, se limpian las especialidades.
            // Esto es importante para poder quitar todas las especialidades a un juez.
            juez.getEspecialidades().clear();
        }

        // Actualizar Sede
        if (request.getSedeId() != null) {
            Sede sede = sedeRepository.findById(request.getSedeId())
                    .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada con ID: " + request.getSedeId()));
            juez.setSede(sede);
        }

        Juez updatedJuez = juezRepository.save(juez);
        return mapToJuezResponse(updatedJuez);
    }

    @Override
    @Transactional
    public void deleteJuez(@NonNull Long id) {
        if (!juezRepository.existsById(id)) {
            throw new IllegalArgumentException("Juez no encontrado con id: " + id);
        }
        juezRepository.deleteById(id);
    }

    @Override
    @Transactional
    public JuezResponse updateJuezProfile(String email, JuezProfileUpdateRequest request, MultipartFile fotoFile) {
        Juez juez = juezRepository.findByCorreo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Juez no encontrado con el correo: " + email));

        // Actualizar los campos permitidos
        juez.setNombre(request.getNombre());

        // Si se envió una nueva foto, subirla y guardar la URL
        if (fotoFile != null && !fotoFile.isEmpty()) {
            try {
                String imageUrl = imageUploadService.uploadImage(fotoFile);
                juez.setFotoUrl(imageUrl);
            } catch (java.io.IOException e) {
                throw new RuntimeException("Error al procesar la imagen de perfil: " + e.getMessage());
            }
        }
        return mapToJuezResponse(juezRepository.save(juez));
    }

    @Override
    @Transactional
    public void suspenderJuez(Long id) {
        Juez juez = juezRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Juez no encontrado con ID: " + id));
        juez.setEstado(JuezEstado.SUSPENDIDO);
        juezRepository.save(juez);
    }

    @Override
    @Transactional
    public void activarJuez(Long id) {
        Juez juez = juezRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Juez no encontrado con ID: " + id));
        juez.setEstado(JuezEstado.ACTIVO);
        juezRepository.save(juez);
    }

    @Override
    @Transactional
    public void retirarJuez(Long id) {
        Juez juez = juezRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Juez no encontrado con ID: " + id));
        juez.setEstado(JuezEstado.RETIRADO);
        juezRepository.save(juez);
    }

    @Override
    @Transactional(readOnly = true)
    public JuezResponse getJuezByCorreo(String correo) {
        Juez juez = juezRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Juez no encontrado con el correo: " + correo));
        return mapToJuezResponse(juez);
    }

    private JuezResponse mapToJuezResponse(Juez juez) {
        // Mapear las especialidades a una lista de nombres
        // Forzamos la inicialización de la colección dentro de la transacción.
        List<String> especialidades = juez.getEspecialidades() != null ?
                juez.getEspecialidades().stream()
                .map(Categoria::getNombre)
                .collect(Collectors.toList()) : java.util.Collections.emptyList();
        
        String nombreSede = juez.getSede() != null ? juez.getSede().getNombre() : "Sin Sede Asignada";
        Long sedeId = juez.getSede() != null ? juez.getSede().getId() : null;

        // Buscar el próximo encuentro asignado al juez
        // Asumimos que Encuentro tiene un campo 'juez' y 'fechaEncuentro'
        // Buscamos encuentros futuros ordenados por fecha ascendente
        List<Encuentro> encuentrosFuturos = encuentroRepository.findByJuezAndFechaEncuentroAfterOrderByFechaEncuentroAsc(juez, LocalDateTime.now());
        
        EncuentroResponse proximoEncuentro = null;
        if (!encuentrosFuturos.isEmpty()) {
            Encuentro encuentro = encuentrosFuturos.get(0);
            proximoEncuentro = new EncuentroResponse(
                encuentro.getId(),
                encuentro.getRobotA().getNombre(),
                encuentro.getRobotA().getId(),
                encuentro.getRobotB().getNombre(),
                encuentro.getRobotB().getId(),
                encuentro.getRobotGanador() != null ? encuentro.getRobotGanador().getNombre() : null,
                encuentro.getRobotGanador() != null ? encuentro.getRobotGanador().getId() : null,
                encuentro.getFechaEncuentro(),
                encuentro.getPuntosRobotA(),
                encuentro.getPuntosRobotB()
            );
        }

        return new JuezResponse(
                juez.getId(),
                juez.getNombre(),
                juez.getDni(),
                juez.getTelefono(),
                juez.getCorreo(),
                juez.getNivelCredencial() != null ? juez.getNivelCredencial().toString() : "NO_ASIGNADO",
                juez.getEstado() != null ? juez.getEstado().toString() : "INDEFINIDO",
                especialidades,
                juez.getFotoUrl(), 
                juez.getFechaCreacion(),
                nombreSede,
                sedeId,
                proximoEncuentro
        );

    }
}