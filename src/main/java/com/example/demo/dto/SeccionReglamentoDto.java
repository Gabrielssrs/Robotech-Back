package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class SeccionReglamentoDto {
    private Long id;
    private Integer numOrden;
    private String tituloMenu;
    private String icono;
    private List<ReglaDto> reglas;
}
