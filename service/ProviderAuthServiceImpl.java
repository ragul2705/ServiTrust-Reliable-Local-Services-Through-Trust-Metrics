package com.servitrust.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.servitrust.dto.AuthResponse;
import com.servitrust.dto.ProviderLoginRequest;
import com.servitrust.dto.ProviderRegisterRequest;
import com.servitrust.model.ProviderAuth;
import com.servitrust.model.ServiceProvider;
import com.servitrust.repository.ProviderAuthRepository;
import com.servitrust.repository.ServiceProviderRepository;

@Service
public class ProviderAuthServiceImpl implements ProviderAuthService {

    @Autowired
    private ProviderAuthRepository providerAuthRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private GeoCodingService geoCodingService;

    private String norm(String s) {
        return s == null ? "" : s.trim();
    }

    private String normEmail(String s) {
        return norm(s).toLowerCase();
    }

    private String normServiceType(String s) {
        return norm(s).toUpperCase().replace(" ", "_");
    }

    @Override
    public AuthResponse register(ProviderRegisterRequest request) {

        String name = norm(request.getName());
        String email = normEmail(request.getEmail());
        String password = norm(request.getPassword());
        String serviceType = normServiceType(request.getServiceType());
        String location = norm(request.getLocation());

        Double lat = request.getLatitude();
        Double lng = request.getLongitude();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || serviceType.isEmpty() || location.isEmpty()) {
            throw new RuntimeException("All fields are required");
        }

        if (providerAuthRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        if ((lat == null || lng == null) && !location.isBlank()) {
            GeoCodingService.LatLng res = geoCodingService.geocode(location);
            if (res != null) {
                lat = res.lat;
                lng = res.lng;
            }
        }

        ProviderAuth auth = new ProviderAuth(
                name,
                email,
                password,
                serviceType,
                location
        );

        ProviderAuth savedAuth = providerAuthRepository.save(auth);

        ServiceProvider sp = new ServiceProvider();
        sp.setAuth(savedAuth);
        sp.setName(name);
        sp.setServiceType(serviceType);
        sp.setLocation(location);
        sp.setLatitude(lat);
        sp.setLongitude(lng);
        sp.setAvailabilityStatus("AVAILABLE");
        sp.setRating(0.0);
        sp.setCompletedJobs(0);
        sp.setAcceptedCount(0);
        sp.setCancelledCount(0);
        sp.setTrustScore(0.0);

        ServiceProvider savedProvider = serviceProviderRepository.save(sp);

        return new AuthResponse(savedAuth.getId(), savedAuth.getName(), "PROVIDER", savedProvider.getId());
    }

    @Override
    public AuthResponse login(ProviderLoginRequest request) {

        String email = normEmail(request.getEmail());
        String password = norm(request.getPassword());

        ProviderAuth auth = providerAuthRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!auth.getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password");
        }

        ServiceProvider sp = serviceProviderRepository
                .findByAuth_Id(auth.getId())
                .orElseThrow(() -> new RuntimeException("Provider record not found"));

        return new AuthResponse(
                auth.getId(),
                auth.getName(),
                "PROVIDER",
                sp.getId()
        );
    }
}