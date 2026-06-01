package com.servitrust.service;

import java.util.List;
import com.servitrust.dto.ReviewRequest;
import com.servitrust.model.ServiceRequest;

public interface ServiceRequestService {

    ServiceRequest createRequest(ServiceRequest request);

    ServiceRequest getRequestById(Long id);

    List<ServiceRequest> getRequestsByUserId(Long userId);

    List<ServiceRequest> getRequestsByProviderId(Long providerId);

    ServiceRequest updateStatus(Long requestId, String status);

    ServiceRequest acceptRequest(Long requestId);

    ServiceRequest completeRequest(Long requestId);

    ServiceRequest cancelByProvider(Long requestId);

    ServiceRequest cancelByUser(Long requestId);

    ServiceRequest submitReview(Long requestId, Long userId, ReviewRequest review);
}