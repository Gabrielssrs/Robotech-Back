package com.example.demo.service;

import com.example.demo.dto.SolicitudRetiroRequest;
import com.example.demo.model.*;
import com.example.demo.repository.ClubRepository;
import com.example.demo.repository.AdministradorRepository;
import com.example.demo.repository.SolicitudRetiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.repository.CompetidorRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudRetiroServiceImpl implements SolicitudRetiroService {

    private final SolicitudRetiroRepository solicitudRetiroRepository;
    private final ClubRepository clubRepository;
    private final CompetidorRepository competidorRepository;
    private final AdministradorRepository administradorRepository;

    @Override
    @Transactional
    public SolicitudRetiro createSolicitud(SolicitudRetiroRequest request, Authentication clubAuthentication) {
        String clubEmail = clubAuthentication.getName();
        Club club = clubRepository.findByCorreo(clubEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Club no encontrado."));

        Competidor competidor = competidorRepository.findByCorreoElectronico(request.getCorreoCompetidor())
                .orElseThrow(() -> new IllegalArgumentException("Competidor no encontrado con el correo: " + request.getCorreoCompetidor()));

        // Verificación de seguridad: el club solo puede solicitar el retiro de sus propios competidores.
        if (!competidor.getClub().getId().equals(club.getId())) {
            throw new SecurityException("No tienes permiso para solicitar el retiro de este competidor.");
        }

        SolicitudRetiro solicitud = new SolicitudRetiro();
        solicitud.setClub(club);
        solicitud.setCompetidor(competidor);
        solicitud.setMotivo(request.getMotivo());

        return solicitudRetiroRepository.save(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudRetiro> getAllSolicitudes() {
        return solicitudRetiroRepository.findAll();
    }

    @Override
    @Transactional
    public SolicitudRetiro aceptarSolicitud(Long solicitudId, Authentication adminAuthentication) {
        SolicitudRetiro solicitud = solicitudRetiroRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud de retiro no encontrada."));

        String adminEmail = adminAuthentication.getName();
        Administrador admin = administradorRepository.findByCorreo(adminEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Administrador no encontrado."));

        // Generar y asignar el código de retiro
        CodigoRetiro codigoRetiro = new CodigoRetiro();
        codigoRetiro.setCodigo(generarCodigoUnico());
        codigoRetiro.setSolicitudRetiro(solicitud);

        // Cambiamos el estado de la solicitud a EN_PROCESO
        solicitud.setAdministradorGestor(admin);
        solicitud.setEstado(SolicitudRetiroEstado.EN_PROCESO);
        solicitud.setCodigoRetiro(codigoRetiro); // Asignamos el código a la solicitud

        // Al guardar la solicitud, el código se guardará por la cascada (CascadeType.ALL)
        return solicitudRetiroRepository.save(solicitud);
    }

    @Override
    @Transactional
    public SolicitudRetiro rechazarSolicitud(Long solicitudId, Authentication adminAuthentication) {
        SolicitudRetiro solicitud = solicitudRetiroRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud de retiro no encontrada."));

        String adminEmail = adminAuthentication.getName();
        Administrador admin = administradorRepository.findByCorreo(adminEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Administrador no encontrado."));

        solicitud.setAdministradorGestor(admin);
        solicitud.setEstado(SolicitudRetiroEstado.RECHAZADO);
        return solicitudRetiroRepository.save(solicitud);
    }

    private String generarCodigoUnico() {
        // Reutilizamos la lógica de generación de código
        String letras = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numeros = "0123456789";
        StringBuilder codigo = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 2; i++) {
            codigo.append(letras.charAt(random.nextInt(letras.length())));
        }
        for (int i = 0; i < 4; i++) {
            codigo.append(numeros.charAt(random.nextInt(numeros.length())));
        }
        return codigo.toString();
    }
}