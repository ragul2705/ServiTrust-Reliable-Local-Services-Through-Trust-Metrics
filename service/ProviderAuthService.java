package com.servitrust.service;

import com.servitrust.dto.AuthResponse;
import com.servitrust.dto.ProviderLoginRequest;
import com.servitrust.dto.ProviderRegisterRequest;

public interface ProviderAuthService {
    AuthResponse register(ProviderRegisterRequest request);
    AuthResponse login(ProviderLoginRequest request);
}