package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import org.springframework.lang.NonNull;

public interface AuthService {
    String login(@NonNull LoginRequest loginRequest);
}