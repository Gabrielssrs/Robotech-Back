package com.example.demo.service;

import com.example.demo.dto.ClubUpdateRequest;
import com.example.demo.model.Club;
import com.example.demo.model.ClubEstado;
import com.example.demo.repository.ClubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAdminProfileClubTest {

    /**
     * MATRIZ DE PRUEBAS - MÉTODO updateClub() [Admin del Club]
     *
     * Caso | Método a Probar | Entrada | Salida Esperada | Observaciones
     * -----|-----------------|---------|-----------------|---------------
     * 1 | updateClub() | Request(nombre="Club Nuevo", tel="912345678", rep="María García", slogan="Nuevo Slogan", cats=["Combate Ligero", "Mini Sumo"], region="Cusco", estado="ACTIVO") | Club actualizado | Todos los campos se actualizan
     * 2 | updateClub() | Request(nombre="Club Actualizado") | Club actualizado | Solo nombre cambia
     * 3 | updateClub() | Request(nombre="Club Original", tel="912345678") | Club actualizado | Solo teléfono cambia
     * 4 | updateClub() | Request(nombre="Club Original", rep="Nuevo Representante") | Club actualizado | Solo representante cambia
     * 5 | updateClub() | Request(nombre="Club Original", estado="SUSPENDIDO") | Club actualizado | Estado se mapea a enum
     * 6 | updateClub() | Request(nombre="Club Original", estado="ESTADO_INVALIDO") | Excepción | IllegalArgumentException al mapear enum
     * 7 | updateClub() | ID=999 | Club = null | Retorna null (sin excepción)
     * 8 | updateClub() | Request(nombre="Club Original", cats=["Sumo Pesado", "Sumo Ligero", "Combate"]) | Club actualizado | Categorías se unen con ", "
     * 9 | updateClub() | Request(nombre="Club Original", cats=[]) | Club actualizado | CategoriasPrincipales se actualiza
     * 10 | updateClub() | Request(nombre="Club"), logoFile válido | Club actualizado | Logo se sube y URL se asigna
     * 11 | updateClub() | Request(nombre="Club Original", tel="91234567a") | Excepción | IllegalArgumentException
     * 12 | updateClub() | Request(nombre="Club Original", tel="91234567") | Excepción | IllegalArgumentException
     * 13 | updateClub() | Request(nombre="Club Original", tel="812345678") | Excepción | IllegalArgumentException
     */

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private ClubServiceImpl clubService;

    private Club existingClub;

    @BeforeEach
    void setUp() {
        existingClub = new Club();
        existingClub.setId(1L);
        existingClub.setNombre("Club Original");
        existingClub.setTelefono("987654321");
        existingClub.setRepresentante("Juan Pérez");
        existingClub.setSlogan("Slogan Original");
        existingClub.setCategoriasPrincipales("Categoría 1, Categoría 2");
        existingClub.setRegion("Lima");
        existingClub.setEstado(ClubEstado.ACTIVO);
        existingClub.setFotoUrl("http://example.com/old-logo.jpg");
    }

    // ============================================
    // PRUEBAS DE ACTUALIZACIÓN DE PERFIL (updateClub)
    // ============================================

    @Test
    /**
     * Propósito: Verifica que el admin del club puede actualizar exitosamente
     * todos los campos del club: nombre, teléfono, representante, slogan, categorías, región y estado.
     * Entrada: request con todos los valores válidos.
     * Resultado esperado: club actualizado con todos los nuevos valores.
     */
    void updateClub_SuccessfulChangeAllFields() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre("Club Nuevo");
        req.setTelefono("912345678");
        req.setRepresentante("María García");
        req.setSlogan("Nuevo Slogan");
        req.setCategorias(List.of("Combate Ligero", "Mini Sumo"));
        req.setRegion("Cusco");
        req.setEstado("ACTIVO");

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Club resp = clubService.updateClub(1L, req, null);

        assertNotNull(resp);
        assertEquals("Club Nuevo", resp.getNombre());
        assertEquals("912345678", resp.getTelefono());
        assertEquals("María García", resp.getRepresentante());
        assertEquals("Nuevo Slogan", resp.getSlogan());
        assertEquals("Combate Ligero, Mini Sumo", resp.getCategoriasPrincipales());
        assertEquals("Cusco", resp.getRegion());
        assertEquals(ClubEstado.ACTIVO, resp.getEstado());
        verify(clubRepository).save(any(Club.class));
        System.out.println("✓ Caso 1: Actualización completa (todos los campos) - EXITOSO");
    }

    @Test
    /**
     * Propósito: Verifica la actualización específica del campo nombre.
     * Entrada: request con el nuevo nombre.
     * Resultado esperado: club con nombre actualizado.
     */
    void updateClub_UpdateOnlyName() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre("Club Actualizado");
        
        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Club resp = clubService.updateClub(1L, req, null);

        assertNotNull(resp);
        assertEquals("Club Actualizado", resp.getNombre());
        verify(clubRepository).save(any(Club.class));
        System.out.println("✓ Caso 2: Solo nombre válido - EXITOSO");
    }

    @Test
    /**
     * Propósito: Verifica la actualización específica del campo teléfono.
     * Entrada: request con el nuevo teléfono.
     * Resultado esperado: club con teléfono actualizado.
     */
    void updateClub_UpdateOnlyTelefono() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre(existingClub.getNombre());
        req.setTelefono("912345678");
        

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Club resp = clubService.updateClub(1L, req, null);

        assertNotNull(resp);
        assertEquals("912345678", resp.getTelefono());
        verify(clubRepository).save(any(Club.class));
        System.out.println("✓ Caso 3: Solo teléfono - EXITOSO");
    }

    @Test
    /**
     * Propósito: Verifica la actualización específica del campo representante.
     * Entrada: request con el nuevo representante.
     * Resultado esperado: club con representante actualizado.
     */
    void updateClub_UpdateOnlyRepresentante() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre(existingClub.getNombre());
        req.setRepresentante("Nuevo Representante");

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Club resp = clubService.updateClub(1L, req, null);

        assertNotNull(resp);
        assertEquals("Nuevo Representante", resp.getRepresentante());
        verify(clubRepository).save(any(Club.class));
        System.out.println("✓ Caso 4: Solo representante - EXITOSO");
    }

    @Test
    /**
     * Propósito: Verifica que el estado se actualiza correctamente cuando se proporciona un valor válido.
     * Entrada: request con estado="SUSPENDIDO".
     * Resultado esperado: club con estado mapeado a ClubEstado.SUSPENDIDO.
     */
    void updateClub_UpdateEstadoValid() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre(existingClub.getNombre());
        req.setEstado("SUSPENDIDO");

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Club resp = clubService.updateClub(1L, req, null);

        assertNotNull(resp);
        assertEquals(ClubEstado.SUSPENDIDO, resp.getEstado());
        verify(clubRepository).save(any(Club.class));
        System.out.println("✓ Caso 5: Estado válido (SUSPENDIDO) - EXITOSO");
    }

    @Test
    /**
     * Propósito: Validar que un estado inválido lance IllegalArgumentException.
     * Entrada: request con estado="ESTADO_INVALIDO".
     * Resultado esperado: excepción IllegalArgumentException.
     */
    void updateClub_UpdateEstadoInvalid_Throws() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre(existingClub.getNombre());
        req.setEstado("ESTADO_INVALIDO");

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));

        assertThrows(IllegalArgumentException.class, () -> clubService.updateClub(1L, req, null));
        System.out.println("✓ Caso 6: Estado inválido - RECHAZADO");
    }

    @Test
    /**
     * Propósito: Verificar que si el club no existe, retorna null.
     * Entrada: id de club inexistente.
     * Resultado esperado: null (sin excepción).
     */
    void updateClub_ClubNotFound_ReturnsNull() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre("Club Nuevo");

        when(clubRepository.findById(999L)).thenReturn(Optional.empty());

        Club resp = clubService.updateClub(999L, req, null);

        assertNull(resp);
        verify(clubRepository, never()).save(any(Club.class));
        System.out.println("✓ Caso 7: Club ID inexistente - RETORNA NULL");
    }

    @Test
    /**
     * Propósito: Verificar que categorías se actualizan correctamente cuando se proporciona lista válida.
     * Entrada: request con categorías=["Cat1", "Cat2", "Cat3"].
     * Resultado esperado: categoriasPrincipales se actualiza con string separado por ", ".
     */
    void updateClub_UpdateCategorias() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre(existingClub.getNombre());
        req.setCategorias(List.of("Sumo Pesado", "Sumo Ligero", "Combate"));

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Club resp = clubService.updateClub(1L, req, null);

        assertNotNull(resp);
        assertEquals("Sumo Pesado, Sumo Ligero, Combate", resp.getCategoriasPrincipales());
        verify(clubRepository).save(any(Club.class));
        System.out.println("✓ Caso 8: Lista de categorías válida - EXITOSO");
    }

    @Test
    /**
     * Propósito: Verificar que categorías vacía se actualiza sin error.
     * Entrada: request con categorías=[] (lista vacía).
     * Resultado esperado: categoriasPrincipales se actualiza (posible vacío por lógica del servicio).
     */
    void updateClub_UpdateCategoriasEmpty() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre(existingClub.getNombre());
        req.setCategorias(List.of());

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Club resp = clubService.updateClub(1L, req, null);

        assertNotNull(resp);
        verify(clubRepository).save(any(Club.class));
        System.out.println("✓ Caso 9: Lista de categorías vacía - EXITOSO");
    }

    @Test
    /**
     * Propósito: Verificar que un logo válido se sube correctamente.
     * Entrada: request con logoFile válido.
     * Resultado esperado: URL del logo se actualiza después del upload.
     */
    void updateClub_UpdateLogo() throws IOException {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre("Club");
        
        MultipartFile logoFile = mock(MultipartFile.class);
        when(logoFile.isEmpty()).thenReturn(false);

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));
        when(imageUploadService.uploadImage(logoFile)).thenReturn("http://example.com/new-logo.jpg");
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Club resp = clubService.updateClub(1L, req, logoFile);

        assertNotNull(resp);
        assertEquals("http://example.com/new-logo.jpg", resp.getFotoUrl());
        verify(imageUploadService).uploadImage(logoFile);
        verify(clubRepository).save(any(Club.class));
        System.out.println("✓ Caso 10: Logo válido - EXITOSO");
    }

    @Test
    void updateClub_PhoneWithLetters_Throws() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre(existingClub.getNombre());
        req.setTelefono("91234567a"); // Contiene letra

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));

        assertThrows(IllegalArgumentException.class, () -> clubService.updateClub(1L, req, null));
        System.out.println("✓ Caso 11: Teléfono con letras - RECHAZADO");
    }

    @Test
    void updateClub_PhoneInvalidLength_Throws() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre(existingClub.getNombre());
        req.setTelefono("91234567"); // 8 dígitos (debe ser 9)

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));

        assertThrows(IllegalArgumentException.class, () -> clubService.updateClub(1L, req, null));
        System.out.println("✓ Caso 12: Teléfono longitud inválida - RECHAZADO");
    }

    @Test
    void updateClub_PhoneNotStartingWithNine_Throws() {
        ClubUpdateRequest req = new ClubUpdateRequest();
        req.setNombre(existingClub.getNombre());
        req.setTelefono("812345678"); // Empieza con 8

        when(clubRepository.findById(1L)).thenReturn(Optional.of(existingClub));

        assertThrows(IllegalArgumentException.class, () -> clubService.updateClub(1L, req, null));
        System.out.println("✓ Caso 13: Teléfono no empieza con 9 - RECHAZADO");
    }

    
}
