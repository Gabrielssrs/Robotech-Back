
package com.example.demo.service;

import com.example.demo.dto.SedeRequest;
import com.example.demo.model.Sede;
import java.util.List;

public interface SedeService {
    Sede createSede(SedeRequest request);
    List<Sede> getAllSedes();
    Sede getSedeById(Long id);
    Sede updateSede(Long id, SedeRequest request);
    void deleteSede(Long id);
}