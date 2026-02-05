package com.example.demo.service;

import com.example.demo.dto.JuezRequest;
import com.example.demo.dto.JuezProfileUpdateRequest;
import com.example.demo.dto.JuezUpdateRequest;
import org.springframework.lang.NonNull;
import com.example.demo.dto.JuezResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface JuezService {
    JuezResponse createJuez(JuezRequest request);
    List<JuezResponse> getAllJueces();
    JuezResponse getJuezById(@NonNull Long id);
    JuezResponse updateJuez(@NonNull Long id, JuezUpdateRequest request);
    JuezResponse updateJuezProfile(String email, JuezProfileUpdateRequest request, MultipartFile fotoFile);
    void deleteJuez(@NonNull Long id);

    void suspenderJuez(Long id);

    void activarJuez(Long id);

    JuezResponse getJuezByCorreo(String correo);

    void retirarJuez(Long id);
}