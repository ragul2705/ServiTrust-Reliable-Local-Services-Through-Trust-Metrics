package com.servitrust.dto;

public class AuthResponse {
    private Long id;          // userId
    private String name;
    private String role;

    private Long providerId;  // ✅ service_providers.id

    public AuthResponse() {}

    public AuthResponse(Long id, String name, String role, Long providerId) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.providerId = providerId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }
}