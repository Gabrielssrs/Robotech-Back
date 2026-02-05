package com.example.demo.dto;

import com.example.demo.model.TipoBloque;
import lombok.Data;

@Data
public class ReglaDto {
    private Long id;
    private String subtitulo;
    private String textoCuerpo;
    private TipoBloque tipoBloque;
    private Double numSuborden;
}
