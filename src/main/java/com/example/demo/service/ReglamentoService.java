package com.example.demo.service;

import com.example.demo.dto.SeccionReglamentoDto;
import java.util.List;

public interface ReglamentoService {
    List<SeccionReglamentoDto> getReglamentoByTorneo(Long torneoId);
    SeccionReglamentoDto createSeccion(Long torneoId, SeccionReglamentoDto dto);
    SeccionReglamentoDto updateSeccion(Long seccionId, SeccionReglamentoDto dto);
    void deleteSeccion(Long seccionId);
}
