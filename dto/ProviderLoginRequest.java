package com.servitrust.dto;

public class ProviderLoginRequest {
    private String email;
    private String password;

    public ProviderLoginRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}