package com.servitrust.model;

import jakarta.persistence.*;

@Entity
@Table(name = "provider_auth")
public class ProviderAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // ✅ THIS FIXES YOUR ERROR
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String serviceType;

    private String location;

    public ProviderAuth() {}

    public ProviderAuth(String name, String email, String password,
                        String serviceType, String location) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.serviceType = serviceType;
        this.location = location;
    }

    public Long getId() { return id; }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public String getServiceType() { return serviceType; }

    public String getLocation() { return location; }
}