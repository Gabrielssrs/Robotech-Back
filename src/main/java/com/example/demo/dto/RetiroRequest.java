package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RetiroRequest {
    @NotBlank(message = "El código de retiro es obligatorio.")
    private String codigoRetiro;
}