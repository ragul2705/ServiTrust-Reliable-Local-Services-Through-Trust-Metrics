package com.servitrust.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.servitrust.model.ServiceProvider;
import com.servitrust.model.User;
import com.servitrust.repository.ServiceProviderRepository;
import com.servitrust.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private ServiceProviderRepository providerRepository;

    // ---------- USERS ----------
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/users/{id}/verify")
    public User verifyUser(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        u.setVerified(true);
        return userRepository.save(u);
    }

    @PutMapping("/users/{id}/unverify")
    public User unverifyUser(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        u.setVerified(false);
        return userRepository.save(u);
    }

    // ---------- PROVIDERS ----------
    @GetMapping("/providers")
    public List<ServiceProvider> getAllProviders() {
        return providerRepository.findAll();
    }

    @PutMapping("/providers/{id}/verify")
    public ServiceProvider verifyProvider(@PathVariable Long id) {
        ServiceProvider p = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        p.setVerified(true);
        return providerRepository.save(p);
    }

    @PutMapping("/providers/{id}/unverify")
    public ServiceProvider unverifyProvider(@PathVariable Long id) {
        ServiceProvider p = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        p.setVerified(false);
        return providerRepository.save(p);
    }
}