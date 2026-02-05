package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SolicitudRetiroRequest {
    @NotBlank @Email
    private String correoCompetidor;
    @NotBlank
    private String motivo;
}