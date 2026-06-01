package com.servitrust.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.servitrust.dto.AuthResponse;
import com.servitrust.dto.ProviderLoginRequest;
import com.servitrust.dto.ProviderRegisterRequest;
import com.servitrust.service.ProviderAuthService;

@RestController
@RequestMapping("/api/providers-auth")
public class ProviderAuthController {

    @Autowired
    private ProviderAuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody ProviderRegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody ProviderLoginRequest req) {
        return authService.login(req);
    }
}