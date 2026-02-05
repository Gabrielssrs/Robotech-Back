package com.example.demo.service;

import com.example.demo.dto.RetiroRequest;
import com.example.demo.dto.CompetidorRegistroClubRequest;
import com.example.demo.dto.CompetidorRegistroRequest;
import com.example.demo.dto.CompetidorUpdateRequest;
import com.example.demo.model.Competidor;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.lang.NonNull;

public interface CompetidorService {
    Competidor registrarCompetidor(@NonNull Competidor competidor, @NonNull String clubEmail);
    Competidor registrarCompetidorPorClub(CompetidorRegistroClubRequest request, String clubEmail);
    Competidor registrarConCodigo(@NonNull CompetidorRegistroRequest request);

    Competidor getCompetidorByEmail(@NonNull String email);

    Competidor suspenderCompetidor(Long id, Authentication authentication);

    Competidor activarCompetidor(Long id, Authentication authentication);

    Competidor retirarCompetidor(Long id, RetiroRequest retiroRequest, Authentication authentication);

    Competidor updateCompetidorProfile(String email, CompetidorUpdateRequest request, MultipartFile fotoFile);
}