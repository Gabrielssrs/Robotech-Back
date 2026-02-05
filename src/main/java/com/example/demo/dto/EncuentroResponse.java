package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EncuentroResponse {
    private Long id;
    private String robotA;
    private Long robotAId;
    private String robotB;
    private Long robotBId;
    private String ganador;
    private Long ganadorId;
    private LocalDateTime fechaHora;
    private Double puntosRobotA;
    private Double puntosRobotB;
}