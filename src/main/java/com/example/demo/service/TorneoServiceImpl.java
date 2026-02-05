package com.example.demo.service;

import com.example.demo.dto.CalificacionRequest;
import com.example.demo.dto.TorneoParticipanteResponse;
import com.example.demo.dto.TorneoRequest;
import com.example.demo.dto.TorneoResponse;
import com.example.demo.dto.EncuentroResponse;
import com.example.demo.model.*;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.JuezRepository;
import com.example.demo.repository.TorneoRepository;
import com.example.demo.repository.SedeRepository;
import com.example.demo.repository.RobotRepository;
import com.example.demo.repository.EncuentroRepository;
import com.example.demo.repository.CalificacionRepository;
import com.example.demo.repository.ResultadoTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TorneoServiceImpl implements TorneoService {

    private final TorneoRepository torneoRepository;
    private final CategoriaRepository categoriaRepository;
    private final JuezRepository juezRepository;
    private final SedeRepository sedeRepository;
    private final RobotRepository robotRepository;
    private final EncuentroRepository encuentroRepository;
    private final CalificacionRepository calificacionRepository;
    private final ResultadoTorneoRepository resultadoTorneoRepository;

    @Override
    @Transactional
    public Torneo createTorneo(TorneoRequest request) {
        try {
            // Validación: Nombre único
            if (torneoRepository.existsByNombre(request.getNombre())) {
                throw new IllegalArgumentException("El nombre del torneo ya existe.");
            }

            // Validación: Máximo 3 torneos iniciados el mismo día
            // Comentado para permitir pruebas ilimitadas
            // List<Torneo> torneosMismoDia = torneoRepository.findByFechaInicio(request.getFechaInicio());
            // if (torneosMismoDia.size() >= 3) {
            //    throw new IllegalArgumentException("Ya existen 3 torneos programados para esta fecha.");
            // }

            Torneo torneo = new Torneo();
            torneo.setNombre(request.getNombre());
            torneo.setDescripcion(request.getDescripcion());
            torneo.setHoraInicio(request.getHoraInicio());

            // Lógica de Fechas Automática
            if (request.getFechaInicioInscripcion() != null && request.getDiasInscripcion() != null) {
                // Comentado para permitir cualquier duración en pruebas
                // if (!List.of(1, 3, 5).contains(request.getDiasInscripcion())) {
                //    throw new IllegalArgumentException("La duración de inscripción debe ser de 1, 3 o 5 días.");
                // }
                LocalDate inicioInscripcion = request.getFechaInicioInscripcion();
                LocalDate finInscripcion = inicioInscripcion.plusDays(request.getDiasInscripcion());
                LocalDate inicioTorneo = finInscripcion; // Modificado: Torneo inicia el mismo día para pruebas
                
                // Cronograma: Octavos (Día 1) -> Cuartos (Día 2) -> Semis (Día 3) -> Final (Día 4)
                // Total duración torneo: 4 días (inicio + 3 días)
                LocalDate finTorneo = inicioTorneo; // Modificado: Todo ocurre el mismo día para pruebas

                torneo.setFechaInicioInscripcion(inicioInscripcion);
                torneo.setFechaLimiteInscripcion(finInscripcion);
                torneo.setFechaInicio(inicioTorneo);
                torneo.setFechaFin(finTorneo);
            } else {
                torneo.setFechaInicio(request.getFechaInicio());
                torneo.setFechaFin(request.getFechaFin());
                torneo.setFechaLimiteInscripcion(request.getFechaLimiteInscripcion());
            }

            Sede sede = sedeRepository.findById(request.getSedeId())
                    .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada con ID: " + request.getSedeId()));
            torneo.setSede(sede);
            // Estado por defecto PROXIMAMENTE si no viene en el request
            torneo.setEstado(request.getEstado() != null ? request.getEstado() : TorneoEstado.PROXIMAMENTE);

            if (request.getCategoriaIds() != null && !request.getCategoriaIds().isEmpty()) {
                List<Categoria> categorias = categoriaRepository.findAllById(request.getCategoriaIds());
                torneo.getCategorias().addAll(categorias);
            }

            if (request.getJuezIds() != null && !request.getJuezIds().isEmpty()) {
                List<Juez> jueces = juezRepository.findAllById(request.getJuezIds());
                torneo.getJueces().addAll(jueces);
            }

            return torneoRepository.save(torneo);
        } catch (Exception e) {
            // Loguear el error para depuración
            System.err.println("Error al crear torneo: " + e.getMessage());
            throw new RuntimeException("Error al crear el torneo: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Torneo updateTorneo(Long id, TorneoRequest request) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado con ID: " + id));

        // Validar nombre único si cambió
        if (request.getNombre() != null && !request.getNombre().equals(torneo.getNombre())) {
            if (torneoRepository.existsByNombre(request.getNombre())) {
                throw new IllegalArgumentException("El nombre del torneo ya existe.");
            }
            torneo.setNombre(request.getNombre());
        }

        if (request.getDescripcion() != null) torneo.setDescripcion(request.getDescripcion());
        if (request.getFechaInicio() != null) torneo.setFechaInicio(request.getFechaInicio());
        if (request.getFechaFin() != null) torneo.setFechaFin(request.getFechaFin());
        if (request.getHoraInicio() != null) torneo.setHoraInicio(request.getHoraInicio());
        if (request.getFechaLimiteInscripcion() != null) torneo.setFechaLimiteInscripcion(request.getFechaLimiteInscripcion());
        
        if (request.getSedeId() != null) {
            Sede sede = sedeRepository.findById(request.getSedeId())
                    .orElseThrow(() -> new IllegalArgumentException("Sede no encontrada con ID: " + request.getSedeId()));
            torneo.setSede(sede);
        }

        if (request.getEstado() != null) torneo.setEstado(request.getEstado());

        if (request.getCategoriaIds() != null) {
            torneo.getCategorias().clear();
            if (!request.getCategoriaIds().isEmpty()) {
                torneo.getCategorias().addAll(categoriaRepository.findAllById(request.getCategoriaIds()));
            }
        }

        if (request.getJuezIds() != null) {
            torneo.getJueces().clear();
            if (!request.getJuezIds().isEmpty()) {
                torneo.getJueces().addAll(juezRepository.findAllById(request.getJuezIds()));
            }
        }

        return torneoRepository.save(torneo);
    }

    @Override
    public List<TorneoResponse> getAllTorneos() {
        return torneoRepository.findAll().stream()
                .map(this::mapToTorneoResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TorneoResponse getTorneoById(Long id) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado con ID: " + id));
        return mapToTorneoResponse(torneo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TorneoParticipanteResponse> getParticipantes(Long torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        List<Encuentro> encuentros = encuentroRepository.findByTorneoId(torneoId);

        return torneo.getParticipantes().stream()
                .map(r -> {
                    String estado = "En espera";
                    // Lógica de Estado
                    if (torneo.getEstado() == TorneoEstado.EN_CURSO || torneo.getEstado() == TorneoEstado.FINALIZADO) {
                        // Verificar si ha sido eliminado (perdió algún partido donde hubo un ganador)
                        boolean eliminado = encuentros.stream()
                                .anyMatch(e -> e.getRobotGanador() != null &&
                                        (e.getRobotA().getId().equals(r.getId()) || e.getRobotB().getId().equals(r.getId())) &&
                                        !e.getRobotGanador().getId().equals(r.getId()));

                        if (eliminado) {
                            estado = "Eliminado";
                        } else {
                            estado = "Participando";
                        }
                    }

                    // Lógica de Puntos: Sumar los puntos obtenidos en los encuentros de este torneo
                    double totalPuntos = encuentros.stream()
                            .mapToDouble(e -> {
                                if (e.getRobotA().getId().equals(r.getId()) && e.getPuntosRobotA() != null) return e.getPuntosRobotA();
                                if (e.getRobotB().getId().equals(r.getId()) && e.getPuntosRobotB() != null) return e.getPuntosRobotB();
                                return 0.0;
                            })
                            .sum();
                    
                    // Redondear a 1 decimal
                    totalPuntos = Math.round(totalPuntos * 10.0) / 10.0;

                    return new TorneoParticipanteResponse(r.getId(), r.getNombre(), r.getCompetidor().getNombre() + " " + r.getCompetidor().getApellido(), totalPuntos, estado, r.getCompetidor().getFotoUrl());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void inscribirRobot(Long torneoId, Long robotId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        if (torneo.getEstado() != TorneoEstado.PROXIMAMENTE) {
            throw new IllegalStateException("El torneo no está en etapa de inscripción.");
        }

        // Validar si el torneo fue cancelado (aunque el estado arriba lo cubre, es bueno ser explícito en el mensaje)
        if (torneo.getEstado() == TorneoEstado.CANCELADO) {
            throw new IllegalStateException("El torneo ha sido cancelado y no acepta inscripciones.");
        }
        
        // Validar fecha límite
        // Comentado para permitir pruebas sin restricciones de fecha
        // if (torneo.getFechaLimiteInscripcion() != null && LocalDate.now().isAfter(torneo.getFechaLimiteInscripcion())) {
        //    throw new IllegalStateException("La fecha límite de inscripción ha pasado.");
        // }

        if (torneo.getParticipantes().size() >= 16) {
            throw new IllegalStateException("El torneo ha alcanzado el límite de 16 participantes.");
        }

        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new IllegalArgumentException("Robot no encontrado"));

        // Validación: Categoría del Robot
        // Verificamos si la categoría del robot está dentro de las categorías permitidas del torneo
        if (!torneo.getCategorias().contains(robot.getCategoria())) {
            throw new IllegalArgumentException("El robot no pertenece a ninguna de las categorías permitidas en este torneo.");
        }

        // Validación: Máximo 2 robots por club
        long robotsDelClub = torneo.getParticipantes().stream()
                .filter(r -> r.getClub().getId().equals(robot.getClub().getId()))
                .count();
        if (robotsDelClub >= 2) {
            throw new IllegalStateException("Tu club ya ha alcanzado el límite máximo de 2 robots inscritos para este torneo.");
        }

        if (torneo.getParticipantes().contains(robot)) {
            throw new IllegalStateException("El robot ya está inscrito en este torneo.");
        }

        torneo.getParticipantes().add(robot);
        torneoRepository.save(torneo);

        if (torneo.getParticipantes().size() == 16) {
            generarFixture(torneo);
        }
    }

    private void generarFixture(Torneo torneo) {
        List<Robot> participantes = new ArrayList<>(torneo.getParticipantes());
        Collections.shuffle(participantes);

        // Modificado para simulación rápida: Los encuentros se programan para AHORA con intervalos de segundos
        LocalDateTime ahora = LocalDateTime.now();

        for (int i = 0; i < 8; i++) {
            Encuentro encuentro = new Encuentro();
            encuentro.setTorneo(torneo);
            encuentro.setRobotA(participantes.get(i * 2));
            encuentro.setRobotB(participantes.get(i * 2 + 1));
            encuentro.setFechaEncuentro(ahora.plusSeconds(i * 2)); // 2 segundos de diferencia para simulación rápida
            encuentroRepository.save(encuentro);
        }

        // Cambiar estado a EN_CURSO (equivalente a "en proceso")
        torneo.setEstado(TorneoEstado.EN_CURSO);
        torneoRepository.save(torneo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EncuentroResponse> getEncuentros(Long torneoId) {
        return encuentroRepository.findByTorneoId(torneoId).stream()
                .sorted(Comparator.comparing(Encuentro::getId))
                .peek(e -> {
                    // Log de depuración para ver si Hibernate está trayendo el ganador
                    if (e.getRobotGanador() != null) {
                        System.out.println("DEBUG READ: Encuentro " + e.getId() + " tiene ganador: " + e.getRobotGanador().getNombre());
                    }
                })
                .map(e -> new EncuentroResponse(e.getId(), 
                        e.getRobotA().getNombre(), 
                        e.getRobotA().getId(),
                        e.getRobotB().getNombre(),
                        e.getRobotB().getId(),
                        e.getRobotGanador() != null ? e.getRobotGanador().getNombre() : null, 
                        e.getRobotGanador() != null ? e.getRobotGanador().getId() : null,
                        e.getFechaEncuentro(),
                        e.getPuntosRobotA(),
                        e.getPuntosRobotB()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EncuentroResponse getEncuentroById(Long id) {
        Encuentro e = encuentroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Encuentro no encontrado con ID: " + id));
        
        return new EncuentroResponse(
                e.getId(),
                e.getRobotA().getNombre(),
                e.getRobotA().getId(),
                e.getRobotB().getNombre(),
                e.getRobotB().getId(),
                e.getRobotGanador() != null ? e.getRobotGanador().getNombre() : null,
                e.getRobotGanador() != null ? e.getRobotGanador().getId() : null,
                e.getFechaEncuentro(),
                e.getPuntosRobotA(),
                e.getPuntosRobotB());
    }

    @Override
    @Transactional
    public void simularInscripcionMasiva(Long torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        if (torneo.getParticipantes().size() >= 16) return;

        // Obtener robots disponibles que no estén ya inscritos
        List<Robot> robotsDisponibles = robotRepository.findAll().stream()
                .filter(r -> !torneo.getParticipantes().contains(r))
                .limit(16 - torneo.getParticipantes().size())
                .collect(Collectors.toList());

        for (Robot robot : robotsDisponibles) {
            torneo.getParticipantes().add(robot);
        }
        torneoRepository.save(torneo);

        if (torneo.getParticipantes().size() == 16 && torneo.getEstado() == TorneoEstado.PROXIMAMENTE) {
            generarFixture(torneo);
        }
    }

    @Override
    @Transactional
    public void registrarGanador(Long encuentroId, Long ganadorId) {
        Encuentro encuentro = encuentroRepository.findById(encuentroId)
                .orElseThrow(() -> new IllegalArgumentException("Encuentro no encontrado"));

        Robot ganador = robotRepository.findById(ganadorId)
                .orElseThrow(() -> new IllegalArgumentException("Robot no encontrado"));

        encuentro.setRobotGanador(ganador);
        encuentroRepository.save(encuentro);

        checkAndGenerateNextRound(encuentro.getTorneo());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TorneoResponse> getTorneosAsignados(String juezEmail) {
        Juez juez = juezRepository.findByCorreo(juezEmail)
                .orElseThrow(() -> new IllegalArgumentException("Juez no encontrado"));

        List<Torneo> torneos = torneoRepository.findByJueces_Correo(juezEmail);

        // Ordenar: EN_CURSO > PROXIMAMENTE > FINALIZADO, luego por fecha
        return torneos.stream()
                .sorted((t1, t2) -> {
                    int p1 = getPrioridadEstado(t1.getEstado());
                    int p2 = getPrioridadEstado(t2.getEstado());
                    if (p1 != p2) return Integer.compare(p1, p2);
                    return t1.getFechaInicio().compareTo(t2.getFechaInicio());
                })
                .map(t -> {
                    // Filtrar categorías para mostrar las que coinciden con la especialidad del juez
                    List<String> categoriasCoincidentes = t.getCategorias().stream()
                            .filter(cat -> juez.getEspecialidades().contains(cat))
                            .map(Categoria::getNombre)
                            .collect(Collectors.toList());
                    
                    // Si no hay coincidencia exacta (ej. juez general), mostramos todas
                    if (categoriasCoincidentes.isEmpty()) {
                        categoriasCoincidentes = t.getCategorias().stream()
                                .map(Categoria::getNombre)
                                .collect(Collectors.toList());
                    }
                    
                    // Usamos un constructor o el mapper adaptado
                    TorneoResponse response = mapToTorneoResponse(t);
                    response.setCategorias(categoriasCoincidentes);
                    return response;
                })
                .collect(Collectors.toList());
    }

    private int getPrioridadEstado(TorneoEstado estado) {
        if (estado == TorneoEstado.EN_CURSO) return 1;
        if (estado == TorneoEstado.PROXIMAMENTE) return 2;
        return 3; // FINALIZADO o CANCELADO
    }

    @Override
    @Transactional(readOnly = true)
    public List<TorneoResponse> getTorneosInscritos(String competidorEmail) {
        return torneoRepository.findByParticipantes_Competidor_CorreoElectronico(competidorEmail).stream()
                .map(this::mapToTorneoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void calificarRobot(String juezEmail, CalificacionRequest request) {
        Juez juez = juezRepository.findByCorreo(juezEmail)
                .orElseThrow(() -> new IllegalArgumentException("Juez no encontrado"));

        Encuentro encuentro = encuentroRepository.findById(request.getEncuentroId())
                .orElseThrow(() -> new IllegalArgumentException("Encuentro no encontrado"));

        Robot robot = robotRepository.findById(request.getRobotId())
                .orElseThrow(() -> new IllegalArgumentException("Robot no encontrado"));

        // Validar que el robot pertenece al encuentro
        if (!encuentro.getRobotA().getId().equals(robot.getId()) && !encuentro.getRobotB().getId().equals(robot.getId())) {
            throw new IllegalArgumentException("El robot no participa en este encuentro.");
        }

        // Buscar si ya existe una calificación de este juez para este robot en este encuentro
        Optional<Calificacion> calificacionExistente = calificacionRepository.findByEncuentroAndRobotAndJuez(encuentro, robot, juez);
        
        Calificacion calificacion = calificacionExistente.orElse(new Calificacion());
        calificacion.setEncuentro(encuentro);
        calificacion.setJuez(juez);
        calificacion.setRobot(robot);
        calificacion.setPuntaje(request.getPuntaje());
        
        calificacionRepository.save(calificacion);

        // Calcular promedio y actualizar encuentro
        List<Calificacion> calificaciones = calificacionRepository.findByEncuentroAndRobot(encuentro, robot);
        double promedio = calificaciones.stream()
                .mapToDouble(Calificacion::getPuntaje)
                .average()
                .orElse(0.0);

        if (encuentro.getRobotA().getId().equals(robot.getId())) {
            encuentro.setPuntosRobotA(promedio);
        } else {
            encuentro.setPuntosRobotB(promedio);
        }
        encuentroRepository.save(encuentro);
    }

    @Override
    @Transactional
    public void simularEncuentro(Long encuentroId) {
        try {
            Encuentro encuentro = encuentroRepository.findById(encuentroId)
                    .orElseThrow(() -> new IllegalArgumentException("Encuentro no encontrado: " + encuentroId));

            if (encuentro.getRobotGanador() != null) {
                System.out.println("INFO: Encuentro " + encuentroId + " ya tiene un ganador. Saltando simulación.");
                return;
            }

            System.out.println("DEBUG: Iniciando simulación para encuentro " + encuentroId);

            List<Juez> jueces = new ArrayList<>(encuentro.getTorneo().getJueces());

            if (jueces.isEmpty()) {
                System.out.println("DEBUG: No hay jueces asignados. Usando simulación simple.");
                double puntosA = Math.round(Math.random() * 100.0) / 10.0;
                double puntosB = Math.round(Math.random() * 100.0) / 10.0;
                while (puntosA == puntosB) puntosB = Math.round(Math.random() * 100.0) / 10.0;
                encuentro.setPuntosRobotA(puntosA);
                encuentro.setPuntosRobotB(puntosB);
                System.out.println("DEBUG: Puntos generados -> Robot A: " + puntosA + ", Robot B: " + puntosB);
            } else {
                System.out.println("DEBUG: Simulando votos para " + jueces.size() + " jueces.");
                for (Juez juez : jueces) {
                    simularVotoJuez(encuentro, encuentro.getRobotA(), juez);
                    simularVotoJuez(encuentro, encuentro.getRobotB(), juez);
                }

                double promedioA = obtenerPromedioCalificaciones(encuentro, encuentro.getRobotA());
                double promedioB = obtenerPromedioCalificaciones(encuentro, encuentro.getRobotB());
                System.out.println("DEBUG: Promedios calculados -> Robot A: " + promedioA + ", Robot B: " + promedioB);

                if (promedioA == promedioB) {
                    promedioB += 0.1;
                    System.out.println("DEBUG: Desempate técnico aplicado. Nuevo promedio B: " + promedioB);
                }

                encuentro.setPuntosRobotA(Math.round(promedioA * 10.0) / 10.0);
                encuentro.setPuntosRobotB(Math.round(promedioB * 10.0) / 10.0);
            }

            Robot ganador = encuentro.getPuntosRobotA() > encuentro.getPuntosRobotB() ? encuentro.getRobotA() : encuentro.getRobotB();
            
            // RE-ADJUNTAR: Aseguramos que la entidad ganador esté gestionada por el EntityManager actual
            ganador = robotRepository.findById(ganador.getId()).orElseThrow();
            encuentro.setRobotGanador(ganador);
            
            System.out.println("DEBUG: Ganador determinado: " + ganador.getNombre());
            
            encuentroRepository.saveAndFlush(encuentro);
            System.out.println("DEBUG: Encuentro " + encuentroId + " guardado con ganador. Procediendo a verificar siguiente ronda.");
            
            checkAndGenerateNextRound(encuentro.getTorneo());
            System.out.println("DEBUG: Simulación de encuentro " + encuentroId + " completada exitosamente.");

        } catch (Exception e) {
            // Log detallado del error
            System.err.println("ERROR CRÍTICO durante la simulación del encuentro " + encuentroId + ": " + e.getMessage());
            e.printStackTrace(); // Imprime el stack trace completo para un diagnóstico profundo
            // Relanzar la excepción para que la transacción haga rollback y el frontend reciba un error 500
            throw new RuntimeException("Fallo en la simulación del encuentro " + encuentroId + ". Causa: " + e.getMessage(), e);
        }
    }

    private void simularVotoJuez(Encuentro encuentro, Robot robot, Juez juez) {
        // Verificar si ya existe calificación para no duplicar en reintentos
        if (calificacionRepository.findByEncuentroAndRobotAndJuez(encuentro, robot, juez).isPresent()) {
            return;
        }

        double puntaje = Math.round(Math.random() * 100.0) / 10.0; // Genera 0.0 a 10.0
        
        Calificacion calificacion = new Calificacion();
        calificacion.setEncuentro(encuentro);
        calificacion.setRobot(robot);
        calificacion.setJuez(juez);
        calificacion.setPuntaje(puntaje);
        
        calificacionRepository.save(calificacion);
    }

    private double obtenerPromedioCalificaciones(Encuentro encuentro, Robot robot) {
        return calificacionRepository.findByEncuentroAndRobot(encuentro, robot).stream()
                .mapToDouble(Calificacion::getPuntaje)
                .average()
                .orElse(0.0);
    }

    private void checkAndGenerateNextRound(Torneo torneo) {
        // OPTIMIZACIÓN: Usar findByTorneoId en lugar de findAll() para evitar traer toda la tabla
        List<Encuentro> encuentros = encuentroRepository.findByTorneoId(torneo.getId()).stream()
                .sorted(Comparator.comparing(Encuentro::getId))
                .collect(Collectors.toList());

        long jugados = encuentros.stream().filter(e -> e.getRobotGanador() != null).count();
        int totalEncuentros = encuentros.size();

        // Lógica simple de progresión: 8 -> 4 -> 2 -> 1
        if (totalEncuentros == 8 && jugados == 8) {
            generarSiguienteRonda(torneo, encuentros, 4); // Generar Cuartos
        } else if (totalEncuentros == 12 && jugados == 12) {
            generarSiguienteRonda(torneo, encuentros.subList(8, 12), 2); // Generar Semis
        } else if (totalEncuentros == 14 && jugados == 14) {
            generarSiguienteRonda(torneo, encuentros.subList(12, 14), 1); // Generar Final
        } else if (totalEncuentros == 15 && jugados == 15) {
            // Finalizó el último partido (la final)
            // Cambiar estado a FINALIZADO
            torneo.setEstado(TorneoEstado.FINALIZADO);
            torneoRepository.save(torneo);

            // Calcular y guardar los 3 primeros puestos
            generarResultadosTorneo(torneo, encuentros);
        }
    }

    private void generarSiguienteRonda(Torneo torneo, List<Encuentro> rondaAnterior, int cantidad) {
        // Modificado para simulación rápida: La siguiente ronda es INMEDIATA (o con 1 min de diferencia)
        // Ignoramos la lógica de "días siguientes" para permitir completar el torneo en segundos.
        LocalDateTime fechaHoraInicio = LocalDateTime.now();

        for (int i = 0; i < cantidad; i++) {
            Encuentro nuevo = new Encuentro();
            nuevo.setTorneo(torneo);
            nuevo.setRobotA(rondaAnterior.get(i * 2).getRobotGanador());
            nuevo.setRobotB(rondaAnterior.get(i * 2 + 1).getRobotGanador());
            nuevo.setFechaEncuentro(fechaHoraInicio.plusSeconds(i * 2)); // Intervalos de segundos
            encuentroRepository.save(nuevo);
        }
    }

    private void generarResultadosTorneo(Torneo torneo, List<Encuentro> encuentros) {
        // El último encuentro de la lista ordenada por ID es la final
        Encuentro finalMatch = encuentros.get(encuentros.size() - 1);

        // 1er Puesto: Ganador de la final
        Robot primerPuesto = finalMatch.getRobotGanador();

        // 2do Puesto: Perdedor de la final
        Robot segundoPuesto = finalMatch.getRobotA().getId().equals(primerPuesto.getId()) 
                ? finalMatch.getRobotB() 
                : finalMatch.getRobotA();

        // 3er Puesto: Robot con más puntos acumulados (excluyendo al 1ro y 2do)
        List<Robot> participantes = new ArrayList<>(torneo.getParticipantes());
        participantes.removeIf(r -> r.getId().equals(primerPuesto.getId()) || r.getId().equals(segundoPuesto.getId()));

        Robot tercerPuesto = participantes.stream()
                .max(Comparator.comparingDouble(r -> calcularPuntosTotales(r, encuentros)))
                .orElse(null);

        ResultadoTorneo resultado = new ResultadoTorneo();
        resultado.setTorneo(torneo);
        resultado.setPrimerPuesto(primerPuesto);
        resultado.setSegundoPuesto(segundoPuesto);
        resultado.setTercerPuesto(tercerPuesto);
        if (tercerPuesto != null) {
            resultado.setPuntosTercerPuesto(calcularPuntosTotales(tercerPuesto, encuentros));
        }

        resultadoTorneoRepository.save(resultado);
    }

    private double calcularPuntosTotales(Robot robot, List<Encuentro> encuentros) {
        return encuentros.stream()
                .mapToDouble(e -> {
                    if (e.getRobotA().getId().equals(robot.getId()) && e.getPuntosRobotA() != null) return e.getPuntosRobotA();
                    if (e.getRobotB().getId().equals(robot.getId()) && e.getPuntosRobotB() != null) return e.getPuntosRobotB();
                    return 0.0;
                })
                .sum();
    }
    
    @Override
    @Transactional
    public void verificarEstadoInscripciones() {
        // Buscar torneos próximos que aún no han comenzado
        List<Torneo> torneos = torneoRepository.findAll().stream()
                .filter(t -> t.getEstado() == TorneoEstado.PROXIMAMENTE)
                .collect(Collectors.toList());
        
        LocalDate hoy = LocalDate.now();
        for (Torneo t : torneos) {
            if (t.getFechaLimiteInscripcion() != null && hoy.isAfter(t.getFechaLimiteInscripcion())) {
                // Si pasó la fecha y no hay suficientes participantes (ej. menos de 16), cancelar
                if (t.getParticipantes().size() < 16) {
                    t.setEstado(TorneoEstado.CANCELADO);
                    torneoRepository.save(t);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoTorneo getResultadoTorneo(Long torneoId) {
        return resultadoTorneoRepository.findAll().stream()
                .filter(r -> r.getTorneo().getId().equals(torneoId))
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional
    public void resetearTorneo(Long torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        // 1. Eliminar resultados finales
        ResultadoTorneo resultado = resultadoTorneoRepository.findAll().stream()
                .filter(r -> r.getTorneo().getId().equals(torneoId))
                .findFirst()
                .orElse(null);
        if (resultado != null) {
            resultadoTorneoRepository.delete(resultado);
        }

        // 2. Eliminar encuentros (y calificaciones por cascada si está configurado en la entidad)
        List<Encuentro> encuentros = encuentroRepository.findByTorneoId(torneoId);
        encuentroRepository.deleteAll(encuentros);

        // 3. Limpiar participantes y resetear estado
        torneo.getParticipantes().clear();
        torneo.setEstado(TorneoEstado.PROXIMAMENTE);
        torneoRepository.save(torneo);
    }

    private TorneoResponse mapToTorneoResponse(Torneo torneo) {
        List<String> categoriasNombres = torneo.getCategorias().stream()
                .map(Categoria::getNombre)
                .collect(Collectors.toList());

        return new TorneoResponse(
                torneo.getId(),
                torneo.getNombre(),
                torneo.getDescripcion(),
                torneo.getFechaInicio(),
                torneo.getHoraInicio(),
                torneo.getFechaFin(),
                torneo.getSede() != null ? torneo.getSede().getNombre() : "Sin Sede",
                torneo.getSede() != null ? torneo.getSede().getId() : null,
                torneo.isActivo(),
                torneo.getEstado().name(),
                categoriasNombres
        );
    }
}