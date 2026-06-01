package com.servitrust.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.servitrust.model.ServiceProvider;
import com.servitrust.repository.ServiceProviderRepository;
import com.servitrust.service.ServiceProviderService;
import com.servitrust.dto.TrustScoreResponse;
import com.servitrust.dto.TrustHistoryItem;

@RestController
@RequestMapping("/api/providers")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderService providerService;

    @Autowired
    private ServiceProviderRepository providerRepository; // ✅ FIX: was missing

    @PostMapping
    public ServiceProvider createProvider(@Valid @RequestBody ServiceProvider provider) {
        return providerService.createProvider(provider);
    }

    @GetMapping("/{id}")
    public ServiceProvider getProviderById(@PathVariable Long id) {
        return providerService.getProviderById(id);
    }

    @GetMapping
    public List<ServiceProvider> getAllProviders() {
        return providerService.getAllProviders();
    }

    @GetMapping("/{id}/trust-score")
    public TrustScoreResponse getTrustScore(@PathVariable Long id) {
        return providerService.getTrustScoreDetails(id);
    }

    @GetMapping("/{id}/trust-history")
    public List<TrustHistoryItem> trustHistory(@PathVariable Long id) {
        return providerService.getTrustHistory(id);
    }

    @GetMapping("/ranking")
    public List<TrustScoreResponse> getProviderRanking() {
        return providerService.getProviderRanking();
    }

    @PostMapping("/{id}/recompute-trust")
    public TrustScoreResponse recomputeTrust(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        String finalReason = (reason == null || reason.trim().isEmpty())
                ? "Manual refresh from provider app"
                : reason.trim();

        providerService.recomputeAndLog(id, finalReason);
        return providerService.getTrustScoreDetails(id);
    }

    // ✅ Search providers by service type and return higher trust first
    @GetMapping("/search")
    public List<ServiceProvider> searchByServiceType(@RequestParam String serviceType) {
        return providerRepository.findByServiceTypeIgnoreCaseOrderByTrustScoreDesc(serviceType.trim());
    }
}