

package com.example.demo.service;

import com.example.demo.model.Solicitud;
import org.springframework.lang.NonNull;

import java.util.List;

public interface SolicitudService {
    Solicitud createSolicitud(Solicitud solicitud, @NonNull Long clubId);
    List<Solicitud> getSolicitudesByClub(String clubEmail);

    void aceptarSolicitud(long solicitudId, String clubEmail);
    void rechazarSolicitud(long solicitudId, String clubEmail);
}