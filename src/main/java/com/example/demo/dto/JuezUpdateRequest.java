package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JuezUpdateRequest {
    // Campo para recibir el nuevo estado como String ("ACTIVO", "RETIRADO", etc.)
    private String estado;
    // Campo para recibir los IDs de las categorías
    private List<Long> categoriaIds;
    private Long sedeId;
}