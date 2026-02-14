package com.example.demo.service;

import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.ResetPasswordRequest;
import org.springframework.lang.NonNull;

public interface AuthService {
    String login(@NonNull LoginRequest loginRequest);
    void requestPasswordReset(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}