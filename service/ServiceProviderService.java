package com.servitrust.service;

import java.util.List;

import com.servitrust.dto.TrustHistoryItem;
import com.servitrust.dto.TrustScoreResponse;
import com.servitrust.model.ServiceProvider;

public interface ServiceProviderService {

    ServiceProvider createProvider(ServiceProvider provider);

    ServiceProvider getProviderById(Long id);

    List<ServiceProvider> getAllProviders();

    TrustScoreResponse getTrustScoreDetails(Long providerId);

    List<TrustScoreResponse> getProviderRanking();

    void recomputeAndLog(Long providerId, String reason);

    List<TrustHistoryItem> getTrustHistory(Long providerId);

    void applyVerifiedComplaint(Long providerId, Long complaintId);
}