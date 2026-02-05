package com.example.demo.service;

import com.example.demo.dto.CategoriaRequest;
import com.example.demo.dto.CategoriaUpdateRequest;
import com.example.demo.model.Categoria;
import com.example.demo.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public Categoria createCategoria(CategoriaRequest request) {
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + request.getNombre());
        }
        Categoria categoria = mapToEntity(request);
        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> getAllCategorias() {
        return categoriaRepository.findAll();
    }

    @Override
    @Transactional
    public Categoria updateCategoria(Long id, CategoriaUpdateRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

        // Mapear los nuevos valores
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        if (request.getTipoCompeticion() != null) {
            categoria.setTipoCompeticion(request.getTipoCompeticion());
        }
        categoria.setPesoMaximoKg(request.getPesoMaximoKg());
        categoria.setAnchoMaximoCm(request.getAnchoMaximoCm());
        categoria.setAltoMaximoCm(request.getAltoMaximoCm());
        categoria.setLargoMaximoCm(request.getLargoMaximoCm());
        // --- CAMPOS AÑADIDOS ---
        categoria.setArmaPrincipalPermitida(request.getArmaPrincipalPermitida());
        categoria.setVelocidadMaximaPermitidaKmh(request.getVelocidadMaximaPermitidaKmh());
        categoria.setTerrenoCompeticion(request.getTerrenoCompeticion());
        categoria.setTipoTraccionPermitido(request.getTipoTraccionPermitido());
        categoria.setMaterialesPermitidos(request.getMaterialesPermitidos());

        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public void deleteCategoria(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new IllegalArgumentException("Categoría no encontrada con ID: " + id);
        }
        categoriaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Categoria habilitarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
        categoria.setActiva(true);
        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public Categoria deshabilitarCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
        categoria.setActiva(false);
        return categoriaRepository.save(categoria);
    }

    private Categoria mapToEntity(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        categoria.setTipoCompeticion(request.getTipoCompeticion());
        categoria.setPesoMaximoKg(request.getPesoMaximoKg());
        categoria.setAnchoMaximoCm(request.getAnchoMaximoCm());
        categoria.setAltoMaximoCm(request.getAltoMaximoCm());
        categoria.setLargoMaximoCm(request.getLargoMaximoCm());
        // --- CAMPOS AÑADIDOS ---
        categoria.setArmaPrincipalPermitida(request.getArmaPrincipalPermitida());
        categoria.setVelocidadMaximaPermitidaKmh(request.getVelocidadMaximaPermitidaKmh());
        categoria.setTerrenoCompeticion(request.getTerrenoCompeticion());
        categoria.setTipoTraccionPermitido(request.getTipoTraccionPermitido());
        categoria.setMaterialesPermitidos(request.getMaterialesPermitidos());
        return categoria;
    }
}