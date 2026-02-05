package com.example.demo.service;

import com.example.demo.dto.SolicitudRetiroRequest;
import com.example.demo.model.SolicitudRetiro;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface SolicitudRetiroService {
    SolicitudRetiro createSolicitud(SolicitudRetiroRequest request, Authentication clubAuthentication);
    List<SolicitudRetiro> getAllSolicitudes();
    SolicitudRetiro aceptarSolicitud(Long solicitudId, Authentication adminAuthentication);
    SolicitudRetiro rechazarSolicitud(Long solicitudId, Authentication adminAuthentication);
}