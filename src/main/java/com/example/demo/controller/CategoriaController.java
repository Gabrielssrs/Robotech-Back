package com.example.demo.controller;

import com.example.demo.dto.CategoriaRequest;
import com.example.demo.dto.CategoriaUpdateRequest;
import com.example.demo.model.Categoria;
import com.example.demo.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Categoria> createCategoria(@Valid @RequestBody CategoriaRequest request) {
        Categoria nuevaCategoria = categoriaService.createCategoria(request);
        return new ResponseEntity<>(nuevaCategoria, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> getAllCategorias() {
        return ResponseEntity.ok(categoriaService.getAllCategorias());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Categoria> updateCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaUpdateRequest request) {
        return ResponseEntity.ok(categoriaService.updateCategoria(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Void> deleteCategoria(@PathVariable Long id) {
        categoriaService.deleteCategoria(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/habilitar")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Categoria> habilitarCategoria(@PathVariable Long id) {
        Categoria categoria = categoriaService.habilitarCategoria(id);
        return ResponseEntity.ok(categoria);
    }

    @PostMapping("/{id}/deshabilitar")
    @PreAuthorize("hasAuthority('ROLE_ADM_SISTEMA')")
    public ResponseEntity<Categoria> deshabilitarCategoria(@PathVariable Long id) {
        Categoria categoria = categoriaService.deshabilitarCategoria(id);
        return ResponseEntity.ok(categoria);
    }
}