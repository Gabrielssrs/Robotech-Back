package com.example.demo.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura las excepciones de validación de negocio (ej: DNI duplicado, correo en uso).
     * Devuelve un error 400 (Bad Request) con un cuerpo JSON que contiene el mensaje específico.
     * Esto asegura que el frontend siempre reciba un JSON y pueda leer la propiedad 'message'.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Creamos un mapa para que la respuesta sea un JSON consistente.
        Map<String, String> errorResponse = Map.of("message", ex.getMessage());
        // Devolvemos un estado 400 Bad Request, que es más apropiado para errores de validación de datos.
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        Map<String, String> errorResponse = Map.of("message", "Error de base de datos: No se pudo completar la operación debido a una restricción de datos.");
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}