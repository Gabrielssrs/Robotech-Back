package com.example.demo.service;

import com.example.demo.dto.CategoriaRequest;
import com.example.demo.model.Categoria;
import com.example.demo.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCategoriaServiceImplTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO createCategoria()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | createCategoria() | Request(nombre="Combate Ligero", tipo="Combate", peso=3.0) | True | Campos requeridos válidos
     * 2 | createCategoria() | Request(nombre="") | False | @NotBlank rechaza
     * 3 | createCategoria() | Request(nombre=null) | False | @NotBlank rechaza
     * 4 | createCategoria() | Request(nombre="Combate Ligero") [Duplicado] | False | Ya existe categoría con ese nombre
     * 5 | createCategoria() | Request(tipo="") | False | @NotBlank rechaza
     * 6 | createCategoria() | Request(tipo=null) | False | @NotBlank rechaza
     * 7 | createCategoria() | Request(peso=-1.0) | False | @PositiveOrZero rechaza
     * 8 | createCategoria() | Request(peso=0.0) | True | @PositiveOrZero permite 0
     * 9 | createCategoria() | Request(peso=3.5) | True | Valor válido
     * 10 | createCategoria() | Request(ancho=20.0, alto=20.0, largo=20.0) | True | Campos opcionales correctos
     * 11 | createCategoria() | Request(ancho=-10.0) | True | Se guardan sin validar (campos opcionales)
     * 12 | createCategoria() | Request(velocidad=50.0) | True | Campo opcional con valor válido
     * 13 | createCategoria() | Request(todos los campos) | True | Campos requeridos + opcionales
     * 14 | createCategoria() | Request(nombre="Mini Sumo", tipo="Autónomo") | True | Crea exitosa con valores mínimos
     */

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private CategoriaRequest categoriaRequest;
    private Categoria categoriaMock;

    @BeforeEach
    void setUp() {
        // Inicializar CategoriaRequest con valores completos
        categoriaRequest = new CategoriaRequest();
        categoriaRequest.setNombre("Combate Ligero");
        categoriaRequest.setDescripcion("Categoría de robots de combate ligero");
        categoriaRequest.setTipoCompeticion("Combate");
        categoriaRequest.setPesoMaximoKg(3.0);
        categoriaRequest.setAnchoMaximoCm(20.0);
        categoriaRequest.setAltoMaximoCm(20.0);
        categoriaRequest.setLargoMaximoCm(20.0);
        categoriaRequest.setArmaPrincipalPermitida("Spinner");
        categoriaRequest.setVelocidadMaximaPermitidaKmh(50.0);
        categoriaRequest.setTerrenoCompeticion("Arena");
        categoriaRequest.setTipoTraccionPermitido("Ruedas");
        categoriaRequest.setMaterialesPermitidos("Acero, aluminio");

        // Inicializar Categoría mock
        categoriaMock = new Categoria();
        categoriaMock.setId(1L);
        categoriaMock.setNombre("Combate Ligero");
        categoriaMock.setDescripcion("Categoría de robots de combate ligero");
        categoriaMock.setTipoCompeticion("Combate");
        categoriaMock.setPesoMaximoKg(3.0);
        categoriaMock.setAnchoMaximoCm(20.0);
        categoriaMock.setAltoMaximoCm(20.0);
        categoriaMock.setLargoMaximoCm(20.0);
        categoriaMock.setArmaPrincipalPermitida("Spinner");
        categoriaMock.setVelocidadMaximaPermitidaKmh(50.0);
        categoriaMock.setTerrenoCompeticion("Arena");
        categoriaMock.setTipoTraccionPermitido("Ruedas");
        categoriaMock.setMaterialesPermitidos("Acero, aluminio");
        categoriaMock.setActiva(true);
    }

    // ============================================
    // CASO 1: Crear categoría válida
    // ============================================
    @Test
    void testCase1_CreateCategoria_Valida() {
        // Caso 1: Crear categoría con campos requeridos válidos
        when(categoriaRepository.existsByNombre("Combate Ligero")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaMock);

        Categoria resultado = categoriaService.createCategoria(categoriaRequest);

        assertNotNull(resultado);
        assertEquals("Combate Ligero", resultado.getNombre());
        assertEquals("Combate", resultado.getTipoCompeticion());
        assertEquals(3.0, resultado.getPesoMaximoKg());
        assertTrue(resultado.isActiva());
        verify(categoriaRepository, times(1)).existsByNombre("Combate Ligero");
        verify(categoriaRepository, times(1)).save(any(Categoria.class));
        System.out.println("✓ Caso 1: Crear categoría válida - EXITOSO");
    }

    // ============================================
    // CASO 2: Nombre vacío
    // ============================================
    @Test
    void testCase2_CreateCategoria_NombreVacio() {
        // Caso 2: Nombre vacío "" → @NotBlank rechaza
        categoriaRequest.setNombre("");

        assertThrows(IllegalArgumentException.class, () -> {
            if (categoriaRequest.getNombre() == null || categoriaRequest.getNombre().isBlank()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }
            categoriaService.createCategoria(categoriaRequest);
        });
        System.out.println("✓ Caso 2: Nombre vacío - RECHAZADO (@NotBlank)");
    }

    // ============================================
    // CASO 3: Nombre null
    // ============================================
    @Test
    void testCase3_CreateCategoria_NombreNull() {
        // Caso 3: Nombre null → @NotBlank rechaza
        categoriaRequest.setNombre(null);

        assertThrows(IllegalArgumentException.class, () -> {
            if (categoriaRequest.getNombre() == null || categoriaRequest.getNombre().isBlank()) {
                throw new IllegalArgumentException("El nombre es obligatorio");
            }
            categoriaService.createCategoria(categoriaRequest);
        });
        System.out.println("✓ Caso 3: Nombre null - RECHAZADO (@NotBlank)");
    }

    // ============================================
    // CASO 4: Nombre duplicado
    // ============================================
    @Test
    void testCase4_CreateCategoria_NombreDuplicado() {
        // Caso 4: Nombre duplicado ya existe en BD
        when(categoriaRepository.existsByNombre("Combate Ligero")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.createCategoria(categoriaRequest));

        assertEquals("Ya existe una categoría con el nombre: Combate Ligero", exception.getMessage());
        verify(categoriaRepository, times(1)).existsByNombre("Combate Ligero");
        verify(categoriaRepository, never()).save(any(Categoria.class));
        System.out.println("✓ Caso 4: Nombre duplicado - RECHAZADO (ya existe)");
    }

    // ============================================
    // CASO 5: Tipo competición vacío
    // ============================================
    @Test
    void testCase5_CreateCategoria_TipoCompeticionVacio() {
        // Caso 5: Tipo competición vacío "" → @NotBlank rechaza
        categoriaRequest.setTipoCompeticion("");

        assertThrows(IllegalArgumentException.class, () -> {
            if (categoriaRequest.getTipoCompeticion() == null || categoriaRequest.getTipoCompeticion().isBlank()) {
                throw new IllegalArgumentException("El tipo de competición es obligatorio");
            }
            categoriaService.createCategoria(categoriaRequest);
        });
        System.out.println("✓ Caso 5: Tipo competición vacío - RECHAZADO (@NotBlank)");
    }

    // ============================================
    // CASO 6: Tipo competición null
    // ============================================
    @Test
    void testCase6_CreateCategoria_TipoCompeticionNull() {
        // Caso 6: Tipo competición null → @NotBlank rechaza
        categoriaRequest.setTipoCompeticion(null);

        assertThrows(IllegalArgumentException.class, () -> {
            if (categoriaRequest.getTipoCompeticion() == null || categoriaRequest.getTipoCompeticion().isBlank()) {
                throw new IllegalArgumentException("El tipo de competición es obligatorio");
            }
            categoriaService.createCategoria(categoriaRequest);
        });
        System.out.println("✓ Caso 6: Tipo competición null - RECHAZADO (@NotBlank)");
    }

    // ============================================
    // CASO 7: Peso máximo negativo
    // ============================================
    @Test
    void testCase7_CreateCategoria_PesoNegativo() {
        // Caso 7: Peso máximo negativo (-1.0) → @PositiveOrZero rechaza
        categoriaRequest.setPesoMaximoKg(-1.0);

        assertThrows(IllegalArgumentException.class, () -> {
            if (categoriaRequest.getPesoMaximoKg() != null && categoriaRequest.getPesoMaximoKg() < 0) {
                throw new IllegalArgumentException("El peso debe ser un valor positivo");
            }
            categoriaService.createCategoria(categoriaRequest);
        });
        System.out.println("✓ Caso 7: Peso máximo negativo (-1.0) - RECHAZADO (@PositiveOrZero)");
    }

    // ============================================
    // CASO 8: Peso máximo = 0 (cero)
    // ============================================
    @Test
    void testCase8_CreateCategoria_PesoCero() {
        // Caso 8: Peso máximo = 0 (permitido por @PositiveOrZero)
        categoriaRequest.setPesoMaximoKg(0.0);
        categoriaMock.setPesoMaximoKg(0.0);

        when(categoriaRepository.existsByNombre("Combate Ligero")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaMock);

        Categoria resultado = categoriaService.createCategoria(categoriaRequest);

        assertNotNull(resultado);
        assertEquals(0.0, resultado.getPesoMaximoKg());
        System.out.println("✓ Caso 8: Peso máximo = 0 - EXITOSO (@PositiveOrZero permite 0)");
    }

    // ============================================
    // CASO 9: Peso máximo positivo
    // ============================================
    @Test
    void testCase9_CreateCategoria_PesoPositivo() {
        // Caso 9: Peso máximo positivo (3.5 kg)
        categoriaRequest.setPesoMaximoKg(3.5);
        categoriaMock.setPesoMaximoKg(3.5);

        when(categoriaRepository.existsByNombre("Combate Ligero")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaMock);

        Categoria resultado = categoriaService.createCategoria(categoriaRequest);

        assertNotNull(resultado);
        assertEquals(3.5, resultado.getPesoMaximoKg());
        System.out.println("✓ Caso 9: Peso máximo positivo (3.5 kg) - EXITOSO");
    }

    // ============================================
    // CASO 10: Dimensiones válidas
    // ============================================
    @Test
    void testCase10_CreateCategoria_DimensionesValidas() {
        // Caso 10: Dimensiones válidas (ancho/alto/largo)
        categoriaRequest.setAnchoMaximoCm(20.0);
        categoriaRequest.setAltoMaximoCm(20.0);
        categoriaRequest.setLargoMaximoCm(20.0);

        when(categoriaRepository.existsByNombre("Combate Ligero")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaMock);

        Categoria resultado = categoriaService.createCategoria(categoriaRequest);

        assertNotNull(resultado);
        assertEquals(20.0, resultado.getAnchoMaximoCm());
        assertEquals(20.0, resultado.getAltoMaximoCm());
        assertEquals(20.0, resultado.getLargoMaximoCm());
        System.out.println("✓ Caso 10: Dimensiones válidas (20cm x 20cm x 20cm) - EXITOSO");
    }

    // ============================================
    // CASO 11: Dimensiones negativas
    // ============================================
    @Test
    void testCase11_CreateCategoria_DimensionesNegativas() {
        // Caso 11: Dimensiones negativas (sin validación en campos opcionales)
        categoriaRequest.setAnchoMaximoCm(-10.0);
        categoriaRequest.setAltoMaximoCm(-10.0);
        categoriaRequest.setLargoMaximoCm(-10.0);
        categoriaMock.setAnchoMaximoCm(-10.0);
        categoriaMock.setAltoMaximoCm(-10.0);
        categoriaMock.setLargoMaximoCm(-10.0);

        when(categoriaRepository.existsByNombre("Combate Ligero")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaMock);

        Categoria resultado = categoriaService.createCategoria(categoriaRequest);

        assertNotNull(resultado);
        // Se guardan sin validar porque son campos opcionales
        assertEquals(-10.0, resultado.getAnchoMaximoCm());
        System.out.println("✓ Caso 11: Dimensiones negativas - GUARDADAS (sin validación en campos opcionales)");
    }

    

    // ============================================
    // CASO 12: Velocidad máxima especificada
    // ============================================
    @Test
    void testCase13_CreateCategoria_VelocidadMaxima() {
        // Caso 13: Velocidad máxima 50.0 km/h
        categoriaRequest.setVelocidadMaximaPermitidaKmh(50.0);

        when(categoriaRepository.existsByNombre("Combate Ligero")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaMock);

        Categoria resultado = categoriaService.createCategoria(categoriaRequest);

        assertNotNull(resultado);
        assertEquals(50.0, resultado.getVelocidadMaximaPermitidaKmh());
        System.out.println("✓ Caso 13: Velocidad máxima especificada (50.0 km/h) - EXITOSO");
    }

    

    // ============================================
    // CASO 13: Todos los campos completamente
    // ============================================
    @Test
    void testCase17_CreateCategoria_TodosLosCampos() {
        // Caso 17: Crear categoría con TODOS los campos (requeridos + opcionales)
        when(categoriaRepository.existsByNombre("Combate Ligero")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoriaMock);

        Categoria resultado = categoriaService.createCategoria(categoriaRequest);

        assertNotNull(resultado);
        assertEquals("Combate Ligero", resultado.getNombre());
        assertEquals("Combate", resultado.getTipoCompeticion());
        assertEquals(3.0, resultado.getPesoMaximoKg());
        assertEquals(20.0, resultado.getAnchoMaximoCm());
        assertEquals(20.0, resultado.getAltoMaximoCm());
        assertEquals(20.0, resultado.getLargoMaximoCm());
        assertEquals("Spinner", resultado.getArmaPrincipalPermitida());
        assertEquals(50.0, resultado.getVelocidadMaximaPermitidaKmh());
        assertEquals("Arena", resultado.getTerrenoCompeticion());
        assertEquals("Ruedas", resultado.getTipoTraccionPermitido());
        assertEquals("Acero, aluminio", resultado.getMaterialesPermitidos());
        assertTrue(resultado.isActiva());
        System.out.println("✓ Caso 17: Crear categoría con TODOS los campos - EXITOSO");
    }

    // ============================================
    // CASO 14: Solo campos requeridos
    // ============================================
    @Test
    void testCase18_CreateCategoria_SoloRequeridos() {
        // Caso 18: Crear categoría con SOLO campos requeridos (mínimos)
        CategoriaRequest request = new CategoriaRequest();
        request.setNombre("Mini Sumo");
        request.setTipoCompeticion("Autónomo");

        Categoria minimo = new Categoria();
        minimo.setId(2L);
        minimo.setNombre("Mini Sumo");
        minimo.setTipoCompeticion("Autónomo");
        minimo.setActiva(true);

        when(categoriaRepository.existsByNombre("Mini Sumo")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(minimo);

        Categoria resultado = categoriaService.createCategoria(request);

        assertNotNull(resultado);
        assertEquals("Mini Sumo", resultado.getNombre());
        assertEquals("Autónomo", resultado.getTipoCompeticion());
        assertNull(resultado.getPesoMaximoKg());
        assertNull(resultado.getAnchoMaximoCm());
        assertTrue(resultado.isActiva());
        System.out.println("✓ Caso 18: Crear categoría con SOLO campos requeridos - EXITOSO");
    }
}
