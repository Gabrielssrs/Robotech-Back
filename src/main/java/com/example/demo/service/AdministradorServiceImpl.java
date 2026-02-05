package com.example.demo.service;

import com.example.demo.dto.AdminResponse;
import com.example.demo.dto.AdminUpdateRequest;
import com.example.demo.model.Administrador;
import com.example.demo.repository.AdministradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdministradorServiceImpl implements AdministradorService {

    private final AdministradorRepository administradorRepository;
      

    @Override
    @Transactional(readOnly = true)
    public AdminResponse getAdminByEmail(String email) {
        Administrador admin = administradorRepository.findByCorreo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Administrador no encontrado con el correo: " + email));
        return mapToAdminResponse(admin);
    }

    @Override
    @Transactional
    public AdminResponse updateAdmin(String email, AdminUpdateRequest updateRequest, MultipartFile fotoFile) { // El MultipartFile se ignora
        Administrador admin = administradorRepository.findByCorreo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Administrador no encontrado con el correo: " + email));

        // Validación de unicidad para DNI
        if (updateRequest.getDni() != null && !updateRequest.getDni().equals(admin.getDni())) {
            if (administradorRepository.existsByDni(updateRequest.getDni())) {
                throw new IllegalStateException("El DNI ya está en uso por otro usuario.");
            }
        }

        // Validación de unicidad para Teléfono
        if (updateRequest.getTelefono() != null && !updateRequest.getTelefono().equals(admin.getTelefono())) {
            if (administradorRepository.existsByTelefono(updateRequest.getTelefono())) {
                throw new IllegalStateException("El teléfono ya está en uso por otro usuario.");
            }
        }

        // Actualizar los campos proporcionados en la solicitud
        admin.setNombre(updateRequest.getNombre());
        admin.setDni(updateRequest.getDni());
        admin.setTelefono(updateRequest.getTelefono());

        Administrador updatedAdmin = administradorRepository.save(admin);
        return mapToAdminResponse(updatedAdmin);
    }

    private AdminResponse mapToAdminResponse(Administrador admin) {
        return new AdminResponse(
                admin.getId(), admin.getNombre(), admin.getDni(), admin.getTelefono(), admin.getCorreo(), admin.isEnabled()
                // Se elimina fotoUrl de la respuesta
        );
    }
}