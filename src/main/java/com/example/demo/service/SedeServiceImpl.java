package com.example.demo.service;

import com.example.demo.dto.SedeRequest;
import com.example.demo.model.Sede;
import com.example.demo.model.SedeEstado;
import com.example.demo.repository.SedeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SedeServiceImpl implements SedeService {

    private final SedeRepository sedeRepository;

    @Override
    @Transactional
    public Sede createSede(SedeRequest request) {
        if (sedeRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException("Ya existe una sede con el nombre: " + request.getNombre());
        }
        Sede sede = new Sede();
        mapRequestToSede(request, sede);
        return sedeRepository.save(sede);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sede> getAllSedes() {
        return sedeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Sede getSedeById(Long id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public Sede updateSede(Long id, SedeRequest request) {
        Sede sede = getSedeById(id);
        mapRequestToSede(request, sede);
        return sedeRepository.save(sede);
    }

    @Override
    @Transactional
    public void deleteSede(Long id) {
        if (!sedeRepository.existsById(id)) {
            throw new IllegalArgumentException("Sede no encontrada con ID: " + id);
        }
        sedeRepository.deleteById(id);
    }

    private void mapRequestToSede(SedeRequest request, Sede sede) {
        sede.setNombre(request.getNombre());
        sede.setDireccion(request.getDireccion());
        sede.setCiudad(request.getCiudad());
        sede.setCapacidadTotal(request.getCapacidadTotal());
        sede.setNroCanchas(request.getNroCanchas());


        sede.setTelefonoContacto(request.getTelefonoContacto());
        if (request.getEstado() != null) {
            sede.setEstado(request.getEstado());
        } else if (sede.getEstado() == null) {
            sede.setEstado(SedeEstado.DISPONIBLE);
        }
    }
}