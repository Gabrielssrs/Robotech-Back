package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.model.Rol;
import com.example.demo.model.RolNombre;
import com.example.demo.repository.ClubRepository;

import com.example.demo.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.example.demo.dto.ClubProfileResponse;
import com.example.demo.dto.ClubUpdateRequest;
import com.example.demo.dto.ClubDetailResponse;
import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;
    private final ImageUploadService imageUploadService;

    @Override
    public List<Club> getAllClubs() {
        // Se devuelve la lista de clubes. Jackson usará los getters de la entidad.
        // La entidad Club tiene un getter getEstado() que devuelve el enum.
        // Jackson serializa el enum a su nombre (ej: "ACTIVO"), que es lo que
        // el frontend espera.
        List<Club> clubs = clubRepository.findAll();
        return clubs;
    }

    @Override
    public Club getClubById(@NonNull Long id) {
        return clubRepository.findById(id).orElse(null);
    }

    @Override
    public Club createClub(Club club) {
        // Verificaciones de unicidad antes de guardar
        if (clubRepository.existsByNombre(club.getNombre())) {
            throw new IllegalStateException("El nombre del club ya está en uso.");
        }
        if (clubRepository.existsByCorreo(club.getCorreo())) {
            throw new IllegalStateException("El correo electrónico ya está registrado.");
        }
        if (club.getTelefono() != null && !club.getTelefono().isEmpty() && clubRepository.existsByTelefono(club.getTelefono())) {
            throw new IllegalStateException("El teléfono de contacto ya está en uso.");
        }
        // Encriptar la contraseña antes de guardarla
        String encodedPassword = passwordEncoder.encode(club.getContrasena());
        club.setContrasena(encodedPassword);

        // Asignar el rol de Administrador de Club por defecto a los nuevos registros.
        Rol rolClub = rolRepository.findByRol(RolNombre.ROLE_ADM_CLUB)
                .orElseThrow(() -> new RuntimeException("Error: Rol de club no encontrado."));
        club.setRoles(Set.of(rolClub));

        return clubRepository.save(club);
    }

    @Override
    @Transactional
    public Club updateClub(@NonNull Long id, ClubUpdateRequest clubDetails, MultipartFile logoFile) {
        Club club = clubRepository.findById(id).orElse(null);
        if (club != null) {
            // Actualizar datos de texto
            club.setNombre(clubDetails.getNombre());
            club.setTelefono(clubDetails.getTelefono());
            club.setRepresentante(clubDetails.getRepresentante());
            club.setSlogan(clubDetails.getSlogan());
            
            if (clubDetails.getCategorias() != null && !clubDetails.getCategorias().isEmpty()) {
                String categoriasString = String.join(", ", clubDetails.getCategorias());
                club.setCategoriasPrincipales(categoriasString);
            }

            club.setRegion(clubDetails.getRegion()); // Asumiendo que quieres editar la región también

            // Actualizar estado si se proporciona
            if (clubDetails.getEstado() != null && !clubDetails.getEstado().isBlank()) {
                ClubEstado nuevoEstado = ClubEstado.valueOf(clubDetails.getEstado().toUpperCase());
                club.setEstado(nuevoEstado);
            }

            // Si se envió un nuevo logo, subirlo a Cloudinary y guardar la URL
            if (logoFile != null && !logoFile.isEmpty()) {
                try {
                    String imageUrl = imageUploadService.uploadImage(logoFile);
                    club.setFotoUrl(imageUrl);
                } catch (IOException e) {
                    throw new RuntimeException("Error al procesar la imagen del logo: " + e.getMessage());
                }
            }

            return clubRepository.save(club);
        }
        return null; // O lanzar una excepción de "recurso no encontrado"
    }

    @Override
    public void deleteClub(@NonNull Long id) {
        clubRepository.deleteById(id);
    }

    @Override
    public Club getClubByEmail(String email) {
        return clubRepository.findByCorreo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Club no encontrado con el correo: " + email));
    }

    @Override
    public Club suspenderClub(long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club no encontrado con ID: " + id));
        club.setEstado(ClubEstado.SUSPENDIDO);
        return clubRepository.save(club);
    }

    @Override
    public Club activarClub(long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club no encontrado con ID: " + id));
        club.setEstado(ClubEstado.ACTIVO);
        return clubRepository.save(club);
    }

    // Método nuevo para retirar un club
    @Override
    public Club retirarClub(long id) {
        Club club = clubRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Club no encontrado con ID: " + id));
        club.setEstado(ClubEstado.RETIRADO);
        return clubRepository.save(club);
    }

    @Override
    @Transactional(readOnly = true)
    public ClubDetailResponse getClubDetailsById(@NonNull Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club no encontrado con ID: " + id));
        // La colección de competidores se cargará lazy, pero al estar dentro de @Transactional, funcionará.
        return new ClubDetailResponse(club);
    }

    @Override
    @Transactional(readOnly = true) // Asegura que la sesión de Hibernate esté activa para cargar los competidores
    public ClubProfileResponse getClubProfileByEmail(String email) {
        Club club = getClubByEmail(email); // Reutilizamos el método existente
        // La conversión a DTO se hace aquí, dentro de la transacción
        return new ClubProfileResponse(club);
    }

    @Override
    public byte[] getClubPhoto(Long id) {
        // Este método ya no es la forma principal de obtener la foto.
        // Se podría dejar para compatibilidad o eliminarlo.
        // Por ahora, lo dejamos devolviendo null.
        return null;
    }

    
}