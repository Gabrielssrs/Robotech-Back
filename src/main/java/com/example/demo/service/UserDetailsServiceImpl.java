package com.example.demo.service;

import com.example.demo.repository.AdministradorRepository;
import com.example.demo.repository.ClubRepository;
import com.example.demo.repository.JuezRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.demo.repository.CompetidorRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AdministradorRepository administradorRepository;
    private final ClubRepository clubRepository;
    private final CompetidorRepository competidorRepository;
    private final JuezRepository juezRepository; // Añadimos el repositorio de Juez

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca en los repositorios en orden: Administrador, Club, Competidor y Juez.
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("El nombre de usuario no puede ser nulo o vacío.");
        }

        // El método or() de Optional permite encadenar las búsquedas de forma elegante.
        // Como todas las entidades de usuario implementan UserDetails, se pueden devolver directamente.
        return administradorRepository.findByCorreo(username)
                .<UserDetails>map(admin -> admin) // El cast es implícito, pero esto ayuda a la inferencia de tipos.
                .or(() -> clubRepository.findByCorreo(username)
                        .map(club -> club))
                .or(() -> competidorRepository.findByCorreoElectronico(username)
                        .map(competidor -> competidor))
                .or(() -> juezRepository.findByCorreo(username) // Añadimos la búsqueda de Juez
                        .map(juez -> juez))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + username));
    }
}