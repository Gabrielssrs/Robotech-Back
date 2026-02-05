package com.example.demo.service;

import com.example.demo.dto.CompetidorRegistroRequest;
import com.example.demo.dto.CompetidorRegistroClubRequest;
import com.example.demo.dto.RetiroRequest;
import com.example.demo.dto.CompetidorUpdateRequest;
import com.example.demo.model.*;
import com.example.demo.repository.CodigoAceptacionRepository;
import com.example.demo.repository.CodigoRetiroRepository;
import com.example.demo.repository.CompetidorRepository;
import com.example.demo.repository.RolRepository;
import com.example.demo.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompetidorServiceImpl implements CompetidorService {

    private final CompetidorRepository competidorRepository;
    private final RolRepository rolRepository;
    private final ClubRepository clubRepository;
    private final CodigoAceptacionRepository codigoAceptacionRepository;
    private final CodigoRetiroRepository codigoRetiroRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageUploadService imageUploadService; // Inyectamos el servicio de imágenes

    @Override
    public Competidor registrarCompetidor(@NonNull Competidor competidor, @NonNull String clubEmail) {
        // 1. Validar si el correo o DNI ya existen para evitar duplicados
        String correo = Objects.requireNonNull(competidor.getCorreoElectronico(), "El correo electrónico no puede ser nulo.");
        if (competidorRepository.findByCorreoElectronico(correo).isPresent()) {
            throw new IllegalStateException("El correo electrónico ya está en uso.");
        }

        String dni = Objects.requireNonNull(competidor.getDni(), "El DNI no puede ser nulo.");
        if (competidorRepository.findByDni(dni).isPresent()) {
            throw new IllegalStateException("El DNI ya está registrado.");
        }

        // Añadida validación para teléfono único
        String telefono = Objects.requireNonNull(competidor.getTelefono(), "El teléfono no puede ser nulo.");
        if (competidorRepository.existsByTelefono(telefono)) {
            throw new IllegalStateException("El teléfono ya está en uso por otro competidor.");
        }

        // Añadida validación para alias único (si se proporciona)
        if (competidor.getAlias() != null && !competidor.getAlias().isBlank() && competidorRepository.existsByAlias(competidor.getAlias())) {
            throw new IllegalStateException("El alias ya está en uso. Por favor elige otro.");
        }

        // 2. Buscar el club al que pertenece el administrador que realiza la acción
        Club club = clubRepository.findByCorreo(clubEmail)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró el club para el usuario autenticado."));
        competidor.setClub(club);

        // 3. Encriptar la contraseña
        competidor.setContrasena(passwordEncoder.encode(competidor.getPassword()));

        // 4. Asignar el rol de competidor
        Rol rolCompetidor = rolRepository.findByRol(RolNombre.ROLE_COMPETIDOR)
                .orElseThrow(() -> new RuntimeException("Error: Rol de competidor no encontrado."));
        competidor.setRoles(Set.of(rolCompetidor));

        // 5. Guardar el nuevo competidor en la base de datos
        return competidorRepository.save(competidor);
    }

    @Override
    public Competidor registrarCompetidorPorClub(CompetidorRegistroClubRequest request, String clubEmail) {
        // Mapeamos el DTO a la entidad Competidor
        Competidor competidor = new Competidor();
        competidor.setNombre(request.getNombre());
        competidor.setApellido(request.getApellido());
        competidor.setDni(request.getDni());
        competidor.setTelefono(request.getTelefono());
        competidor.setCorreoElectronico(request.getCorreoElectronico());
        competidor.setDireccion(request.getDireccion());
        competidor.setAlias(request.getAlias());
        competidor.setCategoria(request.getCategoria());
        competidor.setContrasena(request.getContrasena()); // Pasamos la contraseña plana, registrarCompetidor la encriptará

        // Delegamos al método principal que ya contiene las validaciones y asignación de club/rol
        return registrarCompetidor(competidor, clubEmail);
    }

    @Override
    public Competidor registrarConCodigo(@NonNull CompetidorRegistroRequest request) {
        // 1. Validar el código de aceptación
        CodigoAceptacion codigo = codigoAceptacionRepository.findByCodigo(request.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("El código de registro no es válido."));

        // 2. Verificar que el código no haya sido utilizado
        if (codigo.isUtilizado()) {
            throw new IllegalStateException("Este código ya ha sido utilizado.");
        }

        // 3. Verificar que el código no haya expirado
        if (codigo.getFechaExpiracion().isBefore(LocalDate.now())) {
            throw new IllegalStateException("El código de registro ha expirado.");
        }

        // 4. **Verificación de seguridad clave**: El correo del formulario debe coincidir con el de la solicitud original
        if (!codigo.getSolicitud().getCorreoElectronico().equalsIgnoreCase(request.getCorreoElectronico())) {
            throw new SecurityException("El correo electrónico no coincide con el de la solicitud original.");
        }

        // 5. Crear el nuevo competidor
        Competidor competidor = new Competidor();
        competidor.setNombre(request.getNombre());
        competidor.setApellido(request.getApellido());
        competidor.setDni(request.getDni());
        competidor.setTelefono(request.getTelefono());
        competidor.setCorreoElectronico(request.getCorreoElectronico());
        competidor.setDireccion(request.getDireccion());
        competidor.setAlias(request.getAlias());
        competidor.setCategoria(request.getCategoria());
        competidor.setContrasena(passwordEncoder.encode(request.getContrasena()));

        // Asignar el club desde la solicitud original
        competidor.setClub(codigo.getSolicitud().getClub());

        Rol rolCompetidor = rolRepository.findByRol(RolNombre.ROLE_COMPETIDOR)
                .orElseThrow(() -> new RuntimeException("Error: Rol de competidor no encontrado."));
        competidor.setRoles(Set.of(rolCompetidor));

        // 6. Marcar el código como utilizado y guardar ambos
        codigo.setUtilizado(true);
        codigoAceptacionRepository.save(codigo);
        return competidorRepository.save(competidor);
    }

    @Override
    public Competidor getCompetidorByEmail(@NonNull String email) {
        return competidorRepository.findByCorreoElectronico(email)
                .orElseThrow(() -> new UsernameNotFoundException("Competidor no encontrado con el correo: " + email));
    }

    @Override
    @Transactional
    public Competidor updateCompetidorProfile(String email, CompetidorUpdateRequest request, MultipartFile fotoFile) {
        Competidor competidor = getCompetidorByEmail(email);

        // Validar unicidad del alias si ha cambiado
        if (request.getAlias() != null && !request.getAlias().isBlank() && !request.getAlias().equals(competidor.getAlias())) {
            if (competidorRepository.existsByAlias(request.getAlias())) {
                throw new IllegalStateException("El alias ya está en uso. Por favor elige otro.");
            }
        }

        // Validar unicidad del DNI si ha cambiado
        if (request.getDni() != null && !request.getDni().equals(competidor.getDni())) {
            if (competidorRepository.findByDni(request.getDni()).isPresent()) {
                throw new IllegalStateException("El DNI ya está en uso por otro competidor.");
            }
        }

        // Actualizar los campos
        competidor.setNombre(request.getNombre());
        competidor.setApellido(request.getApellido());
        competidor.setAlias(request.getAlias());
        competidor.setTelefono(request.getTelefono());
        competidor.setDni(request.getDni());

        // Si se envió una nueva foto, subirla y guardar la URL
        if (fotoFile != null && !fotoFile.isEmpty()) {
            try {
                String imageUrl = imageUploadService.uploadImage(fotoFile);
                competidor.setFotoUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Error al procesar la imagen de perfil: " + e.getMessage());
            }
        }

        return competidorRepository.save(competidor);
    }

    @Override
    @Transactional
    public Competidor suspenderCompetidor(Long id, Authentication authentication) {
        Competidor competidor = competidorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Competidor no encontrado con ID: " + id));

        // Si no es admin del sistema, verificar que sea el dueño del club
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority(RolNombre.ROLE_ADM_SISTEMA.name()))) {
            if (!competidor.getClub().getCorreo().equals(authentication.getName())) {
                throw new SecurityException("No tienes permiso para modificar este competidor.");
            }
        }

        competidor.setEstado(CompetidorEstado.SUSPENDIDO);
        return competidorRepository.save(competidor);
    }

    @Override
    @Transactional
    public Competidor activarCompetidor(Long id, Authentication authentication) {
        Competidor competidor = competidorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Competidor no encontrado con ID: " + id));

        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority(RolNombre.ROLE_ADM_SISTEMA.name()))) {
            if (!competidor.getClub().getCorreo().equals(authentication.getName())) {
                throw new SecurityException("No tienes permiso para modificar este competidor.");
            }
        }

        competidor.setEstado(CompetidorEstado.ACTIVO);
        return competidorRepository.save(competidor);
    }

    @Override
    @Transactional
    public Competidor retirarCompetidor(Long id, RetiroRequest retiroRequest, Authentication authentication) {
        // 1. Validar que el usuario sea un admin de sistema
        if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority(RolNombre.ROLE_ADM_SISTEMA.name()))) {
            throw new SecurityException("No tienes permiso para realizar esta acción.");
        }

        // 2. Buscar el código de retiro
        CodigoRetiro codigo = codigoRetiroRepository.findByCodigo(retiroRequest.getCodigoRetiro())
                .orElseThrow(() -> new IllegalArgumentException("El código de retiro no es válido."));

        // 3. Validar que el código no esté usado
        if (codigo.isUtilizado()) {
            throw new IllegalStateException("Este código de retiro ya ha sido utilizado.");
        }

        // 4. Validar que el competidor de la solicitud coincida con el ID del competidor a retirar
        SolicitudRetiro solicitud = codigo.getSolicitudRetiro();
        if (!solicitud.getCompetidor().getId().equals(id)) {
            throw new SecurityException("El código no corresponde a este competidor.");
        }

        // 5. Retirar al competidor y actualizar estados
        Competidor competidor = solicitud.getCompetidor();
        competidor.setEstado(CompetidorEstado.RETIRADO);
        codigo.setUtilizado(true);
        solicitud.setEstado(SolicitudRetiroEstado.TERMINADO);

        return competidorRepository.save(competidor);
    }
}