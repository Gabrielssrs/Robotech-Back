package com.example.demo.service;

import com.example.demo.model.Club;
import com.example.demo.model.CodigoAceptacion;
import com.example.demo.model.SolicitudEstado;
import com.example.demo.model.Solicitud;
import com.example.demo.repository.ClubRepository;
import com.example.demo.repository.SolicitudRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final EmailService emailService;
    private final ClubRepository clubRepository;

    @Override
    @Transactional
    public Solicitud createSolicitud(Solicitud solicitud, @NonNull Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club no encontrado con ID: " + clubId));
        solicitud.setClub(club);

        return solicitudRepository.save(solicitud);
    }

    @Override
    public List<Solicitud> getSolicitudesByClub(String clubEmail) {
        Club club = clubRepository.findByCorreo(clubEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Club no encontrado con el correo: " + clubEmail));
        return solicitudRepository.findByClub(club);
    }

    @Override
    @Transactional
    public void aceptarSolicitud(long solicitudId, String clubEmail) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));

        // Verificación de seguridad: el club que acepta es el correcto
        if (!solicitud.getClub().getCorreo().equals(clubEmail)) {
            throw new IllegalStateException("No tienes permiso para modificar esta solicitud.");
        }

        // Crear la nueva entidad CodigoAceptacion
        CodigoAceptacion nuevoCodigo = new CodigoAceptacion();
        nuevoCodigo.setCodigo(generarCodigoUnico());
        nuevoCodigo.setFechaExpiracion(LocalDate.now().plusDays(3));
        nuevoCodigo.setSolicitud(solicitud);

        // Actualizar la solicitud
        solicitud.setEstado(SolicitudEstado.ACEPTADA);
        solicitud.setCodigoAceptacion(nuevoCodigo); // Establecer la relación
        solicitudRepository.save(solicitud);

        // Enviar correo de aceptación
        String asunto = "¡Tu solicitud ha sido aceptada!";
        String texto = String.format(
            "¡Felicidades, %s!\n\nTu solicitud para unirte a un competidor del club '%s' ha sido aprobada.\n\n" +
            "Usa el siguiente código de registro de un solo uso para completar tu inscripción. ¡Expira en 3 días!\n\n" +
            "Código: %s\n\n" +
            "Saludos,\nEl equipo de %s.",
            solicitud.getNombreCompleto(),
            solicitud.getClub().getNombre(),
            nuevoCodigo.getCodigo(),
            solicitud.getClub().getNombre()
        );
        emailService.enviarCorreoSimple(solicitud.getCorreoElectronico(), asunto, texto);
    }

    @Override
    @Transactional
    public void rechazarSolicitud(long solicitudId, String clubEmail) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));

        // Verificación de seguridad
        if (!solicitud.getClub().getCorreo().equals(clubEmail)) {
            throw new IllegalStateException("No tienes permiso para modificar esta solicitud.");
        }

        // Cambiar el estado a RECHAZADA en lugar de eliminarla
        solicitud.setEstado(SolicitudEstado.RECHAZADA);
        solicitudRepository.save(solicitud);

        // Enviar correo de rechazo
        String asunto = "Actualización sobre tu solicitud";
        String texto = String.format(
            "Hola, %s.\n\n" +
            "Te escribimos para informarte que, tras una revisión, tu solicitud para el club '%s' ha sido rechazada en esta ocasión.\n\n" +
            "Agradecemos tu interés.\n\n" +
            "Saludos,\nEl equipo de %s.",
            solicitud.getNombreCompleto(),
            solicitud.getClub().getNombre(),
            solicitud.getClub().getNombre()
        );
        emailService.enviarCorreoSimple(solicitud.getCorreoElectronico(), asunto, texto);
    }

    private String generarCodigoUnico() {
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
        // En una app real, se debería verificar en la BD si el código ya existe y regenerarlo si es necesario.
        return codigo.toString();
    }
}