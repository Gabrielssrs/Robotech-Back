package com.example.demo.model;

public enum SolicitudRetiroEstado {
    PENDIENTE,     // La solicitud ha sido enviada por el club.
    EN_PROCESO,    // El admin ha aceptado, pero aún no ha finalizado el retiro.
    TERMINADO,     // El competidor ha sido retirado exitosamente.
    RECHAZADO      // El admin ha denegado la solicitud.
}