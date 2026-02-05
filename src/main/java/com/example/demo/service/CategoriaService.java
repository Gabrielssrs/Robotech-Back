package com.example.demo.service;

import com.example.demo.dto.CategoriaRequest;
import com.example.demo.dto.CategoriaUpdateRequest;
import com.example.demo.model.Categoria;

import java.util.List;

public interface CategoriaService {
    Categoria createCategoria(CategoriaRequest categoriaRequest);

    List<Categoria> getAllCategorias();

    Categoria updateCategoria(Long id, CategoriaUpdateRequest categoriaRequest);

    void deleteCategoria(Long id);

    Categoria habilitarCategoria(Long id);

    Categoria deshabilitarCategoria(Long id);
}