package com.example.demo.service;

import com.example.demo.dto.RobotRequest;
import com.example.demo.model.*;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.CompetidorRepository;
import com.example.demo.repository.RobotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RobotServiceImpl implements RobotService {

    private final RobotRepository robotRepository;
    private final CompetidorRepository competidorRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public Robot createRobot(RobotRequest request, Authentication authentication) {
        String email = authentication.getName();
        Competidor competidor = competidorRepository.findByCorreoElectronico(email)
                .orElseThrow(() -> new IllegalArgumentException("Competidor no encontrado"));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Validar que el robot cumpla con las reglas de la categoría
        validarLimitesCategoria(request, categoria);

        Robot robot = new Robot();
        robot.setNombre(request.getNombre());
        robot.setDescripcion(request.getDescripcion());
        robot.setCompetidor(competidor);
        robot.setClub(competidor.getClub());
        robot.setEstado(RobotEstado.ACTIVO);

        // Asignar categoría única
        robot.setCategoria(categoria);

        // Configurar componentes (Relaciones OneToOne)
        configurarComponentes(robot, request);

        return robotRepository.save(robot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Robot> getRobots(Authentication authentication) {
        String email = authentication.getName();
        // Si es admin, podría ver todos (opcional), si es competidor, solo los suyos
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADM_SISTEMA"))) {
            return robotRepository.findAll();
        }
        
        Competidor competidor = competidorRepository.findByCorreoElectronico(email)
                .orElseThrow(() -> new IllegalArgumentException("Competidor no encontrado"));
        return robotRepository.findByCompetidor(competidor);
    }

    @Override
    @Transactional(readOnly = true)
    public Robot getRobotById(Long id, Authentication authentication) {
        Robot robot = robotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Robot no encontrado"));
        
        validarPropiedad(robot, authentication);
        return robot;
    }

    @Override
    @Transactional
    public Robot updateRobot(Long id, RobotRequest request, Authentication authentication) {
        Robot robot = getRobotById(id, authentication); // Valida existencia y propiedad

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        validarLimitesCategoria(request, categoria);

        robot.setNombre(request.getNombre());
        robot.setDescripcion(request.getDescripcion());
        
        // Actualizar categoría
        robot.setCategoria(categoria);

        // Actualizar componentes
        if (robot.getAtaque() == null) robot.setAtaque(new Ataque());
        robot.getAtaque().setArmaPrincipal(request.getAtaque().getArmaPrincipal());
        robot.getAtaque().setArmaSecundaria(request.getAtaque().getArmaSecundaria());
        robot.getAtaque().setAlcance(request.getAtaque().getAlcance());
        robot.getAtaque().setRobot(robot);

        if (robot.getMovilidad() == null) robot.setMovilidad(new Movilidad());
        robot.getMovilidad().setVelocidadMaximaKmh(request.getMovilidad().getVelocidadMaximaKmh());
        robot.getMovilidad().setTipoTraccion(request.getMovilidad().getTipoTraccion());
        robot.getMovilidad().setAgilidad(request.getMovilidad().getAgilidad());
        robot.getMovilidad().setTerrenoIdeal(request.getMovilidad().getTerrenoIdeal());
        robot.getMovilidad().setRobot(robot);

        if (robot.getCaracteristicas() == null) robot.setCaracteristicas(new Caracteristicas());
        robot.getCaracteristicas().setMaterialPrincipal(request.getCaracteristicas().getMaterialPrincipal());
        robot.getCaracteristicas().setPesoKg(request.getCaracteristicas().getPesoKg());
        robot.getCaracteristicas().setAlturaCm(request.getCaracteristicas().getAlturaCm());
        robot.getCaracteristicas().setAnchoCm(request.getCaracteristicas().getAnchoCm());
        robot.getCaracteristicas().setFuentePoder(request.getCaracteristicas().getFuentePoder());
        robot.getCaracteristicas().setBlindaje(request.getCaracteristicas().getBlindaje());
        robot.getCaracteristicas().setCaracteristicaEspecial(request.getCaracteristicas().getCaracteristicaEspecial());
        robot.getCaracteristicas().setRobot(robot);

        return robotRepository.save(robot);
    }

    @Override
    @Transactional
    public void deleteRobot(Long id, Authentication authentication) {
        Robot robot = getRobotById(id, authentication);
        robotRepository.delete(robot);
    }

    // --- Métodos Auxiliares ---

    private void configurarComponentes(Robot robot, RobotRequest request) {
        Ataque ataque = new Ataque();
        ataque.setArmaPrincipal(request.getAtaque().getArmaPrincipal());
        ataque.setArmaSecundaria(request.getAtaque().getArmaSecundaria());
        ataque.setAlcance(request.getAtaque().getAlcance());
        ataque.setRobot(robot);
        robot.setAtaque(ataque);

        Movilidad movilidad = new Movilidad();
        movilidad.setVelocidadMaximaKmh(request.getMovilidad().getVelocidadMaximaKmh());
        movilidad.setTipoTraccion(request.getMovilidad().getTipoTraccion());
        movilidad.setAgilidad(request.getMovilidad().getAgilidad());
        movilidad.setTerrenoIdeal(request.getMovilidad().getTerrenoIdeal());
        movilidad.setRobot(robot);
        robot.setMovilidad(movilidad);

        Caracteristicas caracteristicas = new Caracteristicas();
        caracteristicas.setMaterialPrincipal(request.getCaracteristicas().getMaterialPrincipal());
        caracteristicas.setPesoKg(request.getCaracteristicas().getPesoKg());
        caracteristicas.setAlturaCm(request.getCaracteristicas().getAlturaCm());
        caracteristicas.setAnchoCm(request.getCaracteristicas().getAnchoCm());
        caracteristicas.setFuentePoder(request.getCaracteristicas().getFuentePoder());
        caracteristicas.setBlindaje(request.getCaracteristicas().getBlindaje());
        caracteristicas.setCaracteristicaEspecial(request.getCaracteristicas().getCaracteristicaEspecial());
        caracteristicas.setRobot(robot);
        robot.setCaracteristicas(caracteristicas);
    }

    private void validarLimitesCategoria(RobotRequest request, Categoria cat) {
        if (cat.getPesoMaximoKg() != null && request.getCaracteristicas().getPesoKg() != null 
                && request.getCaracteristicas().getPesoKg() > cat.getPesoMaximoKg()) {
            throw new IllegalArgumentException("El peso del robot (" + request.getCaracteristicas().getPesoKg() + 
                    "kg) excede el máximo permitido por la categoría (" + cat.getPesoMaximoKg() + "kg).");
        }
        if (cat.getVelocidadMaximaPermitidaKmh() != null && request.getMovilidad().getVelocidadMaximaKmh() != null
                && request.getMovilidad().getVelocidadMaximaKmh() > cat.getVelocidadMaximaPermitidaKmh()) {
            throw new IllegalArgumentException("La velocidad excede el límite de la categoría (" + cat.getVelocidadMaximaPermitidaKmh() + " km/h).");
        }
        if (cat.getAltoMaximoCm() != null && request.getCaracteristicas().getAlturaCm() != null
                && request.getCaracteristicas().getAlturaCm() > cat.getAltoMaximoCm()) {
            throw new IllegalArgumentException("La altura excede el límite de la categoría (" + cat.getAltoMaximoCm() + " cm).");
        }
        if (cat.getAnchoMaximoCm() != null && request.getCaracteristicas().getAnchoCm() != null
                && request.getCaracteristicas().getAnchoCm() > cat.getAnchoMaximoCm()) {
            throw new IllegalArgumentException("El ancho excede el límite de la categoría (" + cat.getAnchoMaximoCm() + " cm).");
        }
    }

    private void validarPropiedad(Robot robot, Authentication authentication) {
        String email = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADM_SISTEMA"));
        
        if (!isAdmin && !robot.getCompetidor().getCorreoElectronico().equals(email)) {
            throw new IllegalArgumentException("No tienes permiso para acceder a este robot.");
        }
    }
}