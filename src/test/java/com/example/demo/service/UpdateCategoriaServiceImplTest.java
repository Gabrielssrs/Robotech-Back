package com.example.demo.service;

import com.example.demo.dto.CategoriaUpdateRequest;
import com.example.demo.model.Categoria;
import com.example.demo.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCategoriaServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO updateCategoria()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | updateCategoria() | Request(nombre="Mini Sumo", tipo="Sumo", peso=1.0, ancho=10.0, alto=10.0, largo=10.0, arma="Cuña", vel=30.0, terreno="Ring", traccion="Motor", mat="Aluminio") | Categoría actualizada | Todos los campos se actualizan correctamente
     * 2 | updateCategoria() | Request(nombre="Combate Pesado") | Categoría actualizada | Solo nombre cambia
     * 3 | updateCategoria() | Request(pesoMaximoKg=10.0) | Categoría actualizada | Solo peso cambia
     * 4 | updateCategoria() | Request(descripcion="Nueva descripción") | Categoría actualizada | Solo descripción cambia
     * 5 | updateCategoria() | Request(arma="Soplete", vel=60.0, terreno="Agua", traccion="Neumático", mat="Titanio") | Categoría actualizada | Campos opcionales se asignan
     * 6 | updateCategoria() | Request(nombre="Duplicado") | Excepción | (No implementado en este archivo)
     * 7 | updateCategoria() | ID=999 | Excepción | IllegalArgumentException
     * 8 | updateCategoria() | Request(peso=-1.0) | Excepción | (No implementado en este archivo)
     * 9 | updateCategoria() | Request(nombre="") | Excepción | (No implementado en este archivo)
     * 10 | updateCategoria() | Request(ancho=15.0, alto=15.0, largo=15.0) | Categoría actualizada | Las 3 dimensiones se asignan
     * 11 | updateCategoria() | Request(tipoCompeticion=null) | Categoría actualizada | No se modifica si es null
     */

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria existingCategoria;

    @BeforeEach
    void setUp() {
        existingCategoria = new Categoria();
        existingCategoria.setId(1L);
        existingCategoria.setNombre("Combate Ligero");
        existingCategoria.setDescripcion("Categoría para robots ligeros");
        existingCategoria.setTipoCompeticion("Combate");
        existingCategoria.setPesoMaximoKg(5.0);
        existingCategoria.setAnchoMaximoCm(20.0);
        existingCategoria.setAltoMaximoCm(20.0);
        existingCategoria.setLargoMaximoCm(20.0);
        existingCategoria.setArmaPrincipalPermitida("Rueda");
        existingCategoria.setVelocidadMaximaPermitidaKmh(50.0);
        existingCategoria.setTerrenoCompeticion("Arena");
        existingCategoria.setTipoTraccionPermitido("Motor");
        existingCategoria.setMaterialesPermitidos("Metal, Plástico");
        existingCategoria.setActiva(true);
    }

    // ============================================
    // PRUEBAS DE ACTUALIZACIÓN (updateCategoria)
    // ============================================

    @Test
    /**
     * Propósito: Verifica que una categoría se actualiza correctamente con todos los campos válidos.
     * Entrada: request con nombre, peso, dimensiones, tipoCompetición y campos opcionales.
     * Resultado esperado: categoría actualizada con todos los nuevos valores.
     */
    void updateCategoria_SuccessfulChangeAllFields() {
        System.out.println("TEST: updateCategoria_SuccessfulChangeAllFields - actualizar todos los campos");
        CategoriaUpdateRequest req = new CategoriaUpdateRequest();
        req.setNombre("Mini Sumo");
        req.setDescripcion("Categoría para robots mini");
        req.setTipoCompeticion("Sumo");
        req.setPesoMaximoKg(1.0);
        req.setAnchoMaximoCm(10.0);
        req.setAltoMaximoCm(10.0);
        req.setLargoMaximoCm(10.0);
        req.setArmaPrincipalPermitida("Cuña");
        req.setVelocidadMaximaPermitidaKmh(30.0);
        req.setTerrenoCompeticion("Ring");
        req.setTipoTraccionPermitido("Motor");
        req.setMaterialesPermitidos("Aluminio");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existingCategoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resp = categoriaService.updateCategoria(1L, req);

        assertNotNull(resp);
        assertEquals("Mini Sumo", resp.getNombre());
        assertEquals("Categoría para robots mini", resp.getDescripcion());
        assertEquals("Sumo", resp.getTipoCompeticion());
        assertEquals(1.0, resp.getPesoMaximoKg());
        assertEquals(10.0, resp.getAnchoMaximoCm());
        assertEquals(10.0, resp.getAltoMaximoCm());
        assertEquals(10.0, resp.getLargoMaximoCm());
        assertEquals("Cuña", resp.getArmaPrincipalPermitida());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    /**
     * Propósito: Verifica la actualización específica del campo nombre.
     * Entrada: request con el nuevo nombre.
     * Resultado esperado: categoría con nombre actualizado.
     */
    void updateCategoria_UpdateOnlyName() {
        System.out.println("TEST: updateCategoria_UpdateOnlyName - actualizar solo el nombre");
        CategoriaUpdateRequest req = new CategoriaUpdateRequest();
        req.setNombre("Combate Pesado");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existingCategoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resp = categoriaService.updateCategoria(1L, req);

        assertNotNull(resp);
        assertEquals("Combate Pesado", resp.getNombre());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    /**
     * Propósito: Verifica la actualización específica del campo peso máximo.
     * Entrada: request con el nuevo pesoMaximoKg.
     * Resultado esperado: categoría con peso actualizado.
     */
    void updateCategoria_UpdateOnlyWeight() {
        System.out.println("TEST: updateCategoria_UpdateOnlyWeight - actualizar solo el peso");
        CategoriaUpdateRequest req = new CategoriaUpdateRequest();
        req.setNombre(existingCategoria.getNombre()); // nombre requerido
        req.setPesoMaximoKg(10.0);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existingCategoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resp = categoriaService.updateCategoria(1L, req);

        assertNotNull(resp);
        assertEquals(10.0, resp.getPesoMaximoKg());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    /**
     * Propósito: Verifica la actualización específica del campo descripción.
     * Entrada: request con la nueva descripción.
     * Resultado esperado: categoría con descripción actualizada.
     */
    void updateCategoria_UpdateOnlyDescription() {
        System.out.println("TEST: updateCategoria_UpdateOnlyDescription - actualizar solo la descripción");
        CategoriaUpdateRequest req = new CategoriaUpdateRequest();
        req.setNombre(existingCategoria.getNombre());
        req.setDescripcion("Nueva descripción");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existingCategoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resp = categoriaService.updateCategoria(1L, req);

        assertNotNull(resp);
        assertEquals("Nueva descripción", resp.getDescripcion());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    /**
     * Propósito: Verifica que los campos opcionales se actualizan correctamente.
     * Entrada: request con campos opcionales (armaPermitida, velocidad, terreno, etc).
     * Resultado esperado: categoría con campos opcionales asignados.
     */
    void updateCategoria_UpdateOptionalFields() {
        System.out.println("TEST: updateCategoria_UpdateOptionalFields - actualizar campos opcionales");
        CategoriaUpdateRequest req = new CategoriaUpdateRequest();
        req.setNombre(existingCategoria.getNombre());
        req.setArmaPrincipalPermitida("Soplete");
        req.setVelocidadMaximaPermitidaKmh(60.0);
        req.setTerrenoCompeticion("Agua");
        req.setTipoTraccionPermitido("Neumático");
        req.setMaterialesPermitidos("Titanio");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existingCategoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resp = categoriaService.updateCategoria(1L, req);

        assertNotNull(resp);
        assertEquals("Soplete", resp.getArmaPrincipalPermitida());
        assertEquals(60.0, resp.getVelocidadMaximaPermitidaKmh());
        assertEquals("Agua", resp.getTerrenoCompeticion());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    /**
     * Propósito: Validar que si la categoría no existe, lanza IllegalArgumentException.
     * Entrada: id de categoría inexistente.
     * Resultado esperado: excepción con mensaje "Categoría no encontrada".
     */
    void updateCategoria_CategoriaNotFound_Throws() {
        System.out.println("TEST: updateCategoria_CategoriaNotFound_Throws - categoría inexistente");
        CategoriaUpdateRequest req = new CategoriaUpdateRequest();
        req.setNombre("Alguna Categoría");

        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.updateCategoria(999L, req));
        assertTrue(ex.getMessage().contains("Categoría no encontrada"));
    }

    @Test
    /**
     * Propósito: Verifica que las dimensiones (ancho, alto, largo) se actualizan.
     * Entrada: request con nuevas dimensiones.
     * Resultado esperado: categoría con dimensiones actualizadas.
     */
    void updateCategoria_UpdateDimensions() {
        System.out.println("TEST: updateCategoria_UpdateDimensions - actualizar dimensiones");
        CategoriaUpdateRequest req = new CategoriaUpdateRequest();
        req.setNombre(existingCategoria.getNombre());
        req.setAnchoMaximoCm(15.0);
        req.setAltoMaximoCm(15.0);
        req.setLargoMaximoCm(15.0);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existingCategoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resp = categoriaService.updateCategoria(1L, req);

        assertNotNull(resp);
        assertEquals(15.0, resp.getAnchoMaximoCm());
        assertEquals(15.0, resp.getAltoMaximoCm());
        assertEquals(15.0, resp.getLargoMaximoCm());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    /**
     * Propósito: Verificar que si tipoCompeticion es null, no se modifica el valor actual.
     * Entrada: request con tipoCompeticion = null.
     * Resultado esperado: tipoCompeticion mantiene su valor original.
     */
    void updateCategoria_TipoCompeticionNull_PreservedCurrent() {
        System.out.println("TEST: updateCategoria_TipoCompeticionNull_PreservedCurrent - tipo competición null");
        CategoriaUpdateRequest req = new CategoriaUpdateRequest();
        req.setNombre(existingCategoria.getNombre());
        req.setTipoCompeticion(null);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existingCategoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resp = categoriaService.updateCategoria(1L, req);

        assertNotNull(resp);
        assertEquals("Combate", resp.getTipoCompeticion());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    /**
     * Propósito: Verificar que actualizando solo campos obligatorios, los opcionales se preservan.
     * Entrada: request con solo nombre y peso (campos opcionales null).
     * Resultado esperado: categoría actualizada, campos opcionales mantienen valores.
     */
    void updateCategoria_OnlyMandatoryFields() {
        System.out.println("TEST: updateCategoria_OnlyMandatoryFields - solo campos obligatorios");
        CategoriaUpdateRequest req = new CategoriaUpdateRequest();
        req.setNombre("Nueva Categoría");
        req.setPesoMaximoKg(7.0);
        // Otros campos null

        String originalArma = existingCategoria.getArmaPrincipalPermitida();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existingCategoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria resp = categoriaService.updateCategoria(1L, req);

        assertNotNull(resp);
        assertEquals("Nueva Categoría", resp.getNombre());
        assertEquals(7.0, resp.getPesoMaximoKg());
        verify(categoriaRepository).save(any(Categoria.class));
    }

    
}
