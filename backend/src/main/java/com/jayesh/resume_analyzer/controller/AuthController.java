package com.jayesh.resume_analyzer.controller;

import com.jayesh.resume_analyzer.dto.RegisterRequest;
import com.jayesh.resume_analyzer.dto.UserResponse;
import com.jayesh.resume_analyzer.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.jayesh.resume_analyzer.dto.AuthResponse;
import com.jayesh.resume_analyzer.dto.LoginRequest;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}