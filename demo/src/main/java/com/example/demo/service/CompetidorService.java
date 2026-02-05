package com.example.demo.service;

import com.example.demo.model.Competidor;
import com.example.demo.repository.CompetidorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompetidorService {

    @Autowired
    private CompetidorRepository competidorRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Competidor registrarCompetidor(Competidor competidor) {
        // Verificar si el email ya existe
        if (competidorRepository.findByEmail(competidor.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Verificar si el DNI ya existe
        if (competidorRepository.findByDni(competidor.getDni()).isPresent()) {
            throw new RuntimeException("El DNI ya está registrado");
        }

        // Encriptar la contraseña
        competidor.setPassword(passwordEncoder.encode(competidor.getPassword()));

        // Guardar el competidor
        Competidor savedCompetidor = competidorRepository.save(competidor);

        return savedCompetidor;
    }

    public Optional<Competidor> findByEmail(String email) {
        return competidorRepository.findByEmail(email);
    }
}