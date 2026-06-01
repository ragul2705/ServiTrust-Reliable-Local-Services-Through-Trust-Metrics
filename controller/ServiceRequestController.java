package com.servitrust.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.servitrust.dto.ReviewRequest;
import com.servitrust.dto.ServiceRequestView;
import com.servitrust.model.ServiceRequest;
import com.servitrust.repository.ComplaintRepository;
import com.servitrust.service.ServiceRequestService;

@RestController
@RequestMapping("/api/requests")
public class ServiceRequestController {

    @Autowired
    private ServiceRequestService requestService;
    @Autowired
    private ComplaintRepository complaintRepository; // ✅ FIX: was missing

    @PostMapping
    public ServiceRequest createRequest(@Valid @RequestBody ServiceRequest request) {
        return requestService.createRequest(request);
    }

    @GetMapping("/{id}")
    public ServiceRequest getRequestById(@PathVariable Long id) {
        return requestService.getRequestById(id);
    }

    @GetMapping("/user/{userId}")
    public List<ServiceRequestView> getRequestsByUser(@PathVariable Long userId) {
        List<ServiceRequest> list = requestService.getRequestsByUserId(userId);
        return list.stream().map(r ->
                new ServiceRequestView(
                        r.getId(),
                        r.getStatus(),
                        r.getServiceType(),
                        r.getRating(),
                        r.getFeedback(),
                        complaintRepository.existsByRequestId(r.getId())
                )
        ).toList();
    }

    @GetMapping("/provider/{providerId}")
    public List<ServiceRequest> getRequestsByProvider(@PathVariable Long providerId) {
        return requestService.getRequestsByProviderId(providerId);
    }

    @PutMapping("/{requestId}/status/{status}")
    public ServiceRequest updateStatus(@PathVariable Long requestId, @PathVariable String status) {
        return requestService.updateStatus(requestId, status);
    }

    @PutMapping("/{requestId}/accept")
    public ServiceRequest accept(@PathVariable Long requestId) {
        return requestService.acceptRequest(requestId);
    }

    @PutMapping("/{requestId}/complete")
    public ServiceRequest complete(@PathVariable Long requestId) {
        return requestService.completeRequest(requestId);
    }

    @PutMapping("/{requestId}/cancel-provider")
    public ServiceRequest cancelProvider(@PathVariable Long requestId) {
        return requestService.cancelByProvider(requestId);
    }

    @PutMapping("/{requestId}/cancel-user")
    public ServiceRequest cancelUser(@PathVariable Long requestId) {
        return requestService.cancelByUser(requestId);
    }

    @PostMapping("/{requestId}/review/{userId}")
    public ServiceRequest submitReview(
            @PathVariable Long requestId,
            @PathVariable Long userId,
            @Valid @RequestBody ReviewRequest review
    ) {
        return requestService.submitReview(requestId, userId, review);
    }
}