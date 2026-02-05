package com.example.demo.service;

import com.example.demo.dto.CalificacionRequest;
import com.example.demo.dto.TorneoParticipanteResponse;
import com.example.demo.dto.TorneoRequest;
import com.example.demo.dto.TorneoResponse;
import com.example.demo.dto.EncuentroResponse;
import com.example.demo.model.Torneo;
import com.example.demo.model.ResultadoTorneo;
import java.util.List;

public interface TorneoService {
    Torneo createTorneo(TorneoRequest request);
    Torneo updateTorneo(Long id, TorneoRequest request);
    List<TorneoResponse> getAllTorneos();
    TorneoResponse getTorneoById(Long id);
    void inscribirRobot(Long torneoId, Long robotId);
    List<TorneoParticipanteResponse> getParticipantes(Long torneoId);
    List<EncuentroResponse> getEncuentros(Long torneoId);
    void simularInscripcionMasiva(Long torneoId);
    void registrarGanador(Long encuentroId, Long ganadorId);
    List<TorneoResponse> getTorneosAsignados(String juezEmail);
    List<TorneoResponse> getTorneosInscritos(String competidorEmail);
    void calificarRobot(String juezEmail, CalificacionRequest request);
    void simularEncuentro(Long encuentroId);
    EncuentroResponse getEncuentroById(Long id);
    void verificarEstadoInscripciones();
    ResultadoTorneo getResultadoTorneo(Long torneoId);
    void resetearTorneo(Long torneoId);
}