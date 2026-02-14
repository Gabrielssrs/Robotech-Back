package com.example.demo.service;

import com.example.demo.dto.AdminCreateRequest;
import com.example.demo.dto.AdminResponse;
import com.example.demo.dto.AdminUpdateRequest;
import java.util.List;

public interface SystemAdminService {
    AdminResponse createAdmin(AdminCreateRequest request);
    List<AdminResponse> getAllAdmins();
    AdminResponse updateAdmin(Long id, AdminUpdateRequest request);
    
    void solicitarDesbloqueoEdicion(Long id);
    void aceptarDesbloqueoEdicion(String token);

    void suspenderAdmin(Long id);
    void activarAdmin(Long id);
}