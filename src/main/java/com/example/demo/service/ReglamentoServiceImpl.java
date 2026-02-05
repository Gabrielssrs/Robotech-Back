package com.example.demo.service;

import com.example.demo.dto.ReglaDto;
import com.example.demo.dto.SeccionReglamentoDto;
import com.example.demo.model.Regla;
import com.example.demo.model.SeccionReglamento;
import com.example.demo.model.Torneo;
import com.example.demo.repository.SeccionReglamentoRepository;
import com.example.demo.repository.TorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReglamentoServiceImpl implements ReglamentoService {

    private final SeccionReglamentoRepository seccionRepository;
    private final TorneoRepository torneoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SeccionReglamentoDto> getReglamentoByTorneo(Long torneoId) {
        List<SeccionReglamento> secciones = seccionRepository.findByTorneoIdOrderByNumOrdenAsc(torneoId);
        return secciones.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SeccionReglamentoDto createSeccion(Long torneoId, SeccionReglamentoDto dto) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        SeccionReglamento seccion = new SeccionReglamento();
        seccion.setTorneo(torneo);
        mapDtoToEntity(dto, seccion);

        return mapToDto(seccionRepository.save(seccion));
    }

    @Override
    @Transactional
    public SeccionReglamentoDto updateSeccion(Long seccionId, SeccionReglamentoDto dto) {
        SeccionReglamento seccion = seccionRepository.findById(seccionId)
                .orElseThrow(() -> new IllegalArgumentException("Sección no encontrada"));
        
        mapDtoToEntity(dto, seccion);
        return mapToDto(seccionRepository.save(seccion));
    }

    @Override
    @Transactional
    public void deleteSeccion(Long seccionId) {
        if (!seccionRepository.existsById(seccionId)) {
            throw new IllegalArgumentException("Sección no encontrada");
        }
        seccionRepository.deleteById(seccionId);
    }

    private void mapDtoToEntity(SeccionReglamentoDto dto, SeccionReglamento seccion) {
        seccion.setTituloMenu(dto.getTituloMenu());
        seccion.setNumOrden(dto.getNumOrden());
        seccion.setIcono(dto.getIcono());

        // Limpiar reglas existentes y agregar las nuevas (estrategia simple de reemplazo)
        seccion.getReglas().clear();
        if (dto.getReglas() != null) {
            for (ReglaDto reglaDto : dto.getReglas()) {
                Regla regla = new Regla();
                regla.setSubtitulo(reglaDto.getSubtitulo());
                regla.setTextoCuerpo(reglaDto.getTextoCuerpo());
                regla.setTipoBloque(reglaDto.getTipoBloque());
                regla.setNumSuborden(reglaDto.getNumSuborden());
                regla.setSeccion(seccion); // Establecer relación bidireccional
                seccion.getReglas().add(regla);
            }
        }
    }

    private SeccionReglamentoDto mapToDto(SeccionReglamento seccion) {
        SeccionReglamentoDto dto = new SeccionReglamentoDto();
        dto.setId(seccion.getId());
        dto.setNumOrden(seccion.getNumOrden());
        dto.setTituloMenu(seccion.getTituloMenu());
        dto.setIcono(seccion.getIcono());
        
        List<ReglaDto> reglasDto = seccion.getReglas().stream()
                .map(this::mapReglaToDto)
                .collect(Collectors.toList());
        dto.setReglas(reglasDto);
        return dto;
    }

    private ReglaDto mapReglaToDto(Regla regla) {
        ReglaDto dto = new ReglaDto();
        dto.setId(regla.getId());
        dto.setSubtitulo(regla.getSubtitulo());
        dto.setTextoCuerpo(regla.getTextoCuerpo());
        dto.setTipoBloque(regla.getTipoBloque());
        dto.setNumSuborden(regla.getNumSuborden());
        return dto;
    }
}
