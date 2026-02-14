package com.example.demo.service;

import com.example.demo.dto.JuezUpdateRequest;
import com.example.demo.dto.JuezResponse;
import com.example.demo.model.Categoria;
import com.example.demo.model.Juez;
import com.example.demo.model.JuezEstado;
import com.example.demo.model.Sede;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.EncuentroRepository;
import com.example.demo.repository.JuezRepository;
import com.example.demo.repository.RolRepository;
import com.example.demo.repository.SedeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateJuezByAdminTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO updateJuez()
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | updateJuez() | estado="ACTIVO", categoriaIds=[10,11], sedeId=100 | Juez actualizado | Estado, especialidades y sede cambiadas
     * 2 | updateJuez() | estado="ACTIVO" | Juez actualizado | Solo cambia estado
     * 3 | updateJuez() | categoriaIds=[10] | Juez actualizado | Solo cambian especialidades
     * 4 | updateJuez() | sedeId=100 | Juez actualizado | Solo cambia sede
     * 5 | updateJuez() | categoriaIds=null | Juez actualizado | Especialidades limpiadas
     * 6 | updateJuez() | categoriaIds=[] | Juez actualizado | Especialidades limpiadas
     * 7 | updateJuez() | categoriaIds incluye id inválida | exception | IllegalArgumentException
     * 8 | updateJuez() | sedeId inválido | exception | IllegalArgumentException
     * 9 | updateJuez() | estado inválido | exception | IllegalArgumentException al mapear enum
     * 10 | updateJuez() | estado="" | Juez sin cambio de estado | Estado permanece
     * 11 | updateJuez() | request vacío | Juez actualizado | (implementación actual limpia especialidades)
     * 12 | updateJuez() | id inexistente | exception | IllegalArgumentException
     * 13 | updateJuez() | id = null | exception | NPE/IllegalArgumentException
     */

    @Mock
    private JuezRepository juezRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private SedeRepository sedeRepository;

    @Mock
    private EncuentroRepository encuentroRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private JuezServiceImpl juezService;

    private Juez existingJuez;
    private Categoria cat1;
    private Categoria cat2;
    private Sede sede;

    @BeforeEach
    void setUp() {
        existingJuez = new Juez();
        existingJuez.setId(1L);
        existingJuez.setNombre("Juan Perez");
        existingJuez.setDni("12345678");
        existingJuez.setCorreo("juez@test.com");
        existingJuez.setTelefono("555-1234");
        existingJuez.setEstado(JuezEstado.EN_CAPACITACION);
        existingJuez.setEspecialidades(new HashSet<>());

        cat1 = new Categoria();
        cat1.setId(10L);
        cat1.setNombre("Combate Ligero");

        cat2 = new Categoria();
        cat2.setId(11L);
        cat2.setNombre("Mini Sumo");

        sede = new Sede();
        sede.setId(100L);
        sede.setNombre("Sede Central");
    }

    // ============================================
    // PRUEBAS DE ACTUALIZACIÓN (updateJuez)
    // ============================================

    @Test
    /**
     * Propósito: Verifica que un administrador pueda actualizar correctamente
     * el estado del juez, reemplazar sus especialidades (categorías) y asignar una sede.
     * Entrada: request con `estado="ACTIVO"`, `categoriaIds=[10,11]`, `sedeId=100`.
     * Resultado esperado: el juez queda con estado ACTIVO, 2 especialidades y sede asignada.
     */
    void updateJuez_SuccessfulChangeStateCategoriesSede() {
        // Preparar request para cambiar estado, categorias y sede
        JuezUpdateRequest req = new JuezUpdateRequest();
        req.setEstado("ACTIVO");
        req.setCategoriaIds(List.of(10L, 11L));
        req.setSedeId(100L);

        when(juezRepository.findById(1L)).thenReturn(Optional.of(existingJuez));
        when(categoriaRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(cat1, cat2));
        when(sedeRepository.findById(100L)).thenReturn(Optional.of(sede));
        when(juezRepository.save(any(Juez.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JuezResponse resp = juezService.updateJuez(1L, req);

        assertNotNull(resp);
        assertEquals("ACTIVO", resp.getEstado());
        assertEquals(2, resp.getEspecialidades().size());
        assertEquals(100L, resp.getSedeId());
        verify(juezRepository).save(any(Juez.class));
        System.out.println("✓ Caso 1: Actualización completa (estado, categorías, sede) - EXITOSO");
    }

    @Test
    void updateJuez_UpdateOnlyState() {
        // Caso 2: Solo cambia estado
        JuezUpdateRequest req = new JuezUpdateRequest();
        req.setEstado("ACTIVO");

        when(juezRepository.findById(1L)).thenReturn(Optional.of(existingJuez));
        when(juezRepository.save(any(Juez.class))).thenAnswer(i -> i.getArguments()[0]);

        JuezResponse resp = juezService.updateJuez(1L, req);

        assertEquals("ACTIVO", resp.getEstado());
        verify(juezRepository).save(any(Juez.class));
        System.out.println("✓ Caso 2: Solo cambia estado - EXITOSO");
    }

    @Test
    void updateJuez_UpdateOnlyCategories() {
        // Caso 3: Solo cambian especialidades
        JuezUpdateRequest req = new JuezUpdateRequest();
        req.setCategoriaIds(List.of(10L));

        when(juezRepository.findById(1L)).thenReturn(Optional.of(existingJuez));
        when(categoriaRepository.findAllById(List.of(10L))).thenReturn(List.of(cat1));
        when(juezRepository.save(any(Juez.class))).thenAnswer(i -> i.getArguments()[0]);

        JuezResponse resp = juezService.updateJuez(1L, req);

        assertEquals(1, resp.getEspecialidades().size());
        verify(juezRepository).save(any(Juez.class));
        System.out.println("✓ Caso 3: Solo cambian especialidades - EXITOSO");
    }

    @Test
    void updateJuez_UpdateOnlySede() {
        // Caso 4: Solo cambia sede
        JuezUpdateRequest req = new JuezUpdateRequest();
        req.setSedeId(100L);

        when(juezRepository.findById(1L)).thenReturn(Optional.of(existingJuez));
        when(sedeRepository.findById(100L)).thenReturn(Optional.of(sede));
        when(juezRepository.save(any(Juez.class))).thenAnswer(i -> i.getArguments()[0]);

        JuezResponse resp = juezService.updateJuez(1L, req);

        assertEquals(100L, resp.getSedeId());
        verify(juezRepository).save(any(Juez.class));
        System.out.println("✓ Caso 4: Solo cambia sede - EXITOSO");
    }

    @Test
    /**
     * Propósito: Cuando el admin envía `categoriaIds = null`, el servicio debe limpiar
     * todas las especialidades del juez (se interpreta como "quitar todas").
     * Entrada: request con `categoriaIds = null`.
     * Resultado esperado: la lista de especialidades del juez queda vacía.
     */
    void updateJuez_ClearCategoriesWhenNull() {
        // Cuando categoriaIds == null, service clears especialidades
        JuezUpdateRequest req = new JuezUpdateRequest();
        req.setEstado(null);
        req.setCategoriaIds(null); // should clear
        req.setSedeId(null);

        existingJuez.setEspecialidades(new HashSet<>(Set.of(cat1, cat2)));
        when(juezRepository.findById(1L)).thenReturn(Optional.of(existingJuez));
        when(juezRepository.save(any(Juez.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JuezResponse resp = juezService.updateJuez(1L, req);

        assertNotNull(resp);
        assertTrue(resp.getEspecialidades().isEmpty());
        verify(juezRepository).save(any(Juez.class));
        System.out.println("✓ Caso 5: Limpiar categorías (null) - EXITOSO");
    }

    @Test
    void updateJuez_ClearCategoriesWhenEmptyList() {
        // Caso 6: Especialidades limpiadas (lista vacía)
        JuezUpdateRequest req = new JuezUpdateRequest();
        req.setCategoriaIds(List.of());

        existingJuez.setEspecialidades(new HashSet<>(Set.of(cat1)));
        when(juezRepository.findById(1L)).thenReturn(Optional.of(existingJuez));
        when(juezRepository.save(any(Juez.class))).thenAnswer(i -> i.getArguments()[0]);

        JuezResponse resp = juezService.updateJuez(1L, req);

        assertTrue(resp.getEspecialidades().isEmpty());
        System.out.println("✓ Caso 6: Especialidades limpiadas (lista vacía) - EXITOSO");
    }

    @Test
    /**
     * Propósito: Validar que si alguna ID de categoría no existe, el servicio lance
     * una `IllegalArgumentException` para evitar asignar IDs inválidas.
     * Entrada: request con `categoriaIds` que incluye una ID inexistente.
     * Resultado esperado: excepción con mensaje informativo.
     */
    void updateJuez_InvalidCategoryId_Throws() {
        // category repository returns only one when two ids provided -> mismatch size -> throw
        JuezUpdateRequest req = new JuezUpdateRequest();
        req.setCategoriaIds(List.of(10L, 999L));

        when(juezRepository.findById(1L)).thenReturn(Optional.of(existingJuez));
        when(categoriaRepository.findAllById(List.of(10L, 999L))).thenReturn(List.of(cat1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.updateJuez(1L, req));
        assertTrue(ex.getMessage().contains("Una o más IDs de categoría no son válidas"));
        System.out.println("✓ Caso 7: ID de categoría inválida - RECHAZADO");
    }

    @Test
    /**
     * Propósito: Comprobar que si se proporciona un `sedeId` que no existe,
     * el servicio arroje `IllegalArgumentException` y no modifique nada.
     * Entrada: request con `sedeId` inexistente.
     * Resultado esperado: excepción indicando "Sede no encontrada".
     */
    void updateJuez_InvalidSedeId_Throws() {
        JuezUpdateRequest req = new JuezUpdateRequest();
        req.setSedeId(999L);

        when(juezRepository.findById(1L)).thenReturn(Optional.of(existingJuez));
        when(sedeRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.updateJuez(1L, req));
        assertTrue(ex.getMessage().contains("Sede no encontrada con ID"));
        System.out.println("✓ Caso 8: ID de sede inválido - RECHAZADO");
    }

    @Test
    void updateJuez_InvalidState_Throws() {
        // Caso 9: Estado inválido
        JuezUpdateRequest req = new JuezUpdateRequest();
        req.setEstado("INVALIDO");

        when(juezRepository.findById(1L)).thenReturn(Optional.of(existingJuez));

        assertThrows(IllegalArgumentException.class, () -> juezService.updateJuez(1L, req));
        System.out.println("✓ Caso 9: Estado inválido - RECHAZADO");
    }

    @Test
    void updateJuez_EmptyState_NoChange() {
        // Caso 10: Estado vacío (sin cambios)
        JuezUpdateRequest req = new JuezUpdateRequest();
        req.setEstado("");

        when(juezRepository.findById(1L)).thenReturn(Optional.of(existingJuez));
        when(juezRepository.save(any(Juez.class))).thenAnswer(i -> i.getArguments()[0]);

        JuezResponse resp = juezService.updateJuez(1L, req);

        assertEquals(JuezEstado.EN_CAPACITACION.toString(), resp.getEstado());
        System.out.println("✓ Caso 10: Estado vacío (sin cambios) - EXITOSO");
    }

    @Test
    void updateJuez_EmptyRequest() {
        // Caso 11: Request vacío (limpia especialidades por defecto al ser null)
        JuezUpdateRequest req = new JuezUpdateRequest();

        when(juezRepository.findById(1L)).thenReturn(Optional.of(existingJuez));
        when(juezRepository.save(any(Juez.class))).thenAnswer(i -> i.getArguments()[0]);

        JuezResponse resp = juezService.updateJuez(1L, req);

        assertTrue(resp.getEspecialidades().isEmpty());
        System.out.println("✓ Caso 11: Request vacío (limpia especialidades) - EXITOSO");
    }

    @Test
    /**
     * Propósito: Verificar que si el `id` de juez no existe, el servicio lance
     * `IllegalArgumentException` y no intente realizar actualizaciones.
     * Entrada: id de juez inexistente.
     * Resultado esperado: excepción con mensaje "Juez no encontrado".
     */
    void updateJuez_JuezNotFound_Throws() {
        JuezUpdateRequest req = new JuezUpdateRequest();
        when(juezRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> juezService.updateJuez(999L, req));
        assertTrue(ex.getMessage().contains("Juez no encontrado"));
        System.out.println("✓ Caso 12: Juez no encontrado - RECHAZADO");
    }

    @Test
    void updateJuez_NullId_Throws() {
        // Caso 13: ID nulo
        JuezUpdateRequest req = new JuezUpdateRequest();
        when(juezRepository.findById(null)).thenThrow(new IllegalArgumentException("ID cannot be null"));

        assertThrows(IllegalArgumentException.class, () -> juezService.updateJuez(null, req));
        System.out.println("✓ Caso 13: ID nulo - RECHAZADO");
    }
}
