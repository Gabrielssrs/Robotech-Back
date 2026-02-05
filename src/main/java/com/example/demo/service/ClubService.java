package com.example.demo.service;
import com.example.demo.dto.ClubProfileResponse;
import com.example.demo.dto.ClubUpdateRequest;
import com.example.demo.dto.ClubDetailResponse;
import com.example.demo.model.Club;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ClubService {
    List<Club> getAllClubs();
    Club getClubById(@NonNull Long id);
    Club createClub(Club club);
    Club updateClub(@NonNull Long id, ClubUpdateRequest clubDetails, MultipartFile logoFile);
    void deleteClub(@NonNull Long id);
    Club getClubByEmail(String email);
    Club suspenderClub(long id);
    Club activarClub(long id);
    Club retirarClub(long id);
    ClubDetailResponse getClubDetailsById(@NonNull Long id); // Nuevo método
    ClubProfileResponse getClubProfileByEmail(String email);
    byte[] getClubPhoto(Long id);
}