package com.servitrust.service;

import com.servitrust.logic.TrustScoreCalculator;
import com.servitrust.model.ServiceProvider;
import com.servitrust.repository.ServiceProviderRepository;
import com.servitrust.repository.ServiceRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class TrustScoreServiceImpl implements TrustScoreService {

    private final ServiceProviderRepository providerRepository;
    private final ServiceRequestRepository requestRepository;

    public TrustScoreServiceImpl(ServiceProviderRepository providerRepository,
                                  ServiceRequestRepository requestRepository) {
        this.providerRepository = providerRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public double computeTrustScore(Long providerId) {

        ServiceProvider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        long totalRequests = requestRepository.countByProviderId(providerId);
        long successfulRequests = requestRepository.countByProviderIdAndStatus(providerId, "Completed");
        long cancellations = requestRepository.countByProviderIdAndStatus(providerId, "Cancelled");

        return TrustScoreCalculator.calculate(
                provider.getRating(),
                provider.getCompletedJobs(),
                totalRequests,
                successfulRequests,
                provider.getAvailabilityStatus().equalsIgnoreCase("AVAILABLE"),
                cancellations
        );
    }
}
