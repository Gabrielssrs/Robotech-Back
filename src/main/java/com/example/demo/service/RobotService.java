package com.example.demo.service;

import com.example.demo.dto.RobotRequest;
import com.example.demo.model.Robot;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface RobotService {
    Robot createRobot(RobotRequest request, Authentication authentication);
    List<Robot> getRobots(Authentication authentication);
    Robot getRobotById(Long id, Authentication authentication);
    Robot updateRobot(Long id, RobotRequest request, Authentication authentication);
    void deleteRobot(Long id, Authentication authentication);
}