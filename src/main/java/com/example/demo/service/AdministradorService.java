package com.example.demo.service;

import com.example.demo.dto.AdminResponse;
import com.example.demo.dto.AdminUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AdministradorService {
    AdminResponse getAdminByEmail(String email);

    AdminResponse updateAdmin(String email, AdminUpdateRequest updateRequest, MultipartFile fotoFile);
}