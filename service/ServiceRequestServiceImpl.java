package com.servitrust.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.servitrust.dto.ReviewRequest;
import com.servitrust.model.Review;
import com.servitrust.model.ServiceProvider;
import com.servitrust.model.ServiceRequest;
import com.servitrust.model.User;
import com.servitrust.repository.ReviewRepository;
import com.servitrust.repository.ServiceProviderRepository;
import com.servitrust.repository.ServiceRequestRepository;
import com.servitrust.repository.UserRepository;

@Service
public class ServiceRequestServiceImpl implements ServiceRequestService {

    @Autowired
    private ServiceRequestRepository requestRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ServiceProviderRepository providerRepo;

    @Autowired
    private ReviewRepository reviewRepo;

    @Autowired
    private ServiceProviderServiceImpl providerServiceImpl;

    @Override
    public ServiceRequest createRequest(ServiceRequest request) {
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new RuntimeException("Invalid userId from app");
        }

        if (request.getProviderId() == null || request.getProviderId() <= 0) {
            throw new RuntimeException("Invalid providerId from app");
        }

        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        request.setUserName(user.getName());

        String status = request.getStatus();
        request.setStatus((status == null || status.isBlank()) ? "PENDING" : status.trim().toUpperCase());

        String st = request.getServiceType();
        request.setServiceType(st == null ? "" : st.trim().toUpperCase());

        if (request.getReviewed() == null) request.setReviewed(false);

        // ✅ IMPORTANT: Do NOT recompute trust on new request creation
        return requestRepo.save(request);
    }

    @Override
    public ServiceRequest getRequestById(Long id) {
        return requestRepo.findById(id).orElseThrow(() -> new RuntimeException("Request not found"));
    }

    @Override
    public List<ServiceRequest> getRequestsByUserId(Long userId) {
        return requestRepo.findByUserId(userId);
    }

    @Override
    public List<ServiceRequest> getRequestsByProviderId(Long providerId) {
        return requestRepo.findByProviderId(providerId);
    }

    @Override
    public ServiceRequest updateStatus(Long requestId, String status) {
        ServiceRequest req = getRequestById(requestId);
        req.setStatus(status.trim().toUpperCase());
        ServiceRequest saved = requestRepo.save(req);

        // ✅ Recompute ONLY when outcome affects trust
        String st = saved.getStatus();
        if ("COMPLETED".equalsIgnoreCase(st)
                || "CANCELLED_BY_PROVIDER".equalsIgnoreCase(st)
                || "CANCELLED_BY_USER".equalsIgnoreCase(st)) {
            providerServiceImpl.recomputeAndLog(saved.getProviderId(), "Status changed to " + st);
        }

        return saved;
    }

    @Override
    public ServiceRequest acceptRequest(Long requestId) {
        // ✅ Accept should not change trust
        return updateStatus(requestId, "ACCEPTED");
    }

    @Override
    public ServiceRequest completeRequest(Long requestId) {
        // ✅ Completion changes trust (handled inside updateStatus)
        return updateStatus(requestId, "COMPLETED");
    }

    @Override
    public ServiceRequest cancelByProvider(Long requestId) {
        // ✅ Cancel affects trust (handled inside updateStatus)
        return updateStatus(requestId, "CANCELLED_BY_PROVIDER");
    }

    @Override
    public ServiceRequest cancelByUser(Long requestId) {
        // ✅ Cancel affects trust (handled inside updateStatus)
        return updateStatus(requestId, "CANCELLED_BY_USER");
    }

    @Override
    public ServiceRequest submitReview(Long requestId, Long userId, ReviewRequest review) {
        ServiceRequest req = getRequestById(requestId);

        if (!req.getUserId().equals(userId)) {
            throw new RuntimeException("Not allowed");
        }

        if (!"COMPLETED".equalsIgnoreCase(req.getStatus())) {
            throw new RuntimeException("Review allowed only after completion");
        }

        if (Boolean.TRUE.equals(req.getReviewed())) {
            throw new RuntimeException("Already reviewed");
        }

        if (reviewRepo.existsByRequestId(requestId)) {
            throw new RuntimeException("Already reviewed");
        }

        req.setRating(review.getRating());
        String fb = review.getFeedback();
        req.setFeedback(fb == null ? null : fb.trim());
        req.setReviewed(true);

        ServiceRequest saved = requestRepo.save(req);

        Review r = new Review();
        r.setRequestId(requestId);
        r.setProviderId(req.getProviderId());
        r.setUserId(userId);
        r.setRating(review.getRating() == null ? 0.0 : review.getRating().doubleValue());
        r.setComment(fb == null ? null : fb.trim());
        reviewRepo.save(r);

        ServiceProvider sp = providerRepo.findById(req.getProviderId())
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        Double avg = requestRepo.getAvgRatingForProvider(req.getProviderId());
        if (avg == null) avg = 0.0;

        sp.setRating(avg);
        if (sp.getCompletedJobs() == null) sp.setCompletedJobs(0);
        sp.setCompletedJobs(sp.getCompletedJobs() + 1);

        providerRepo.save(sp);

        // ✅ Review should recompute trust
        providerServiceImpl.recomputeAndLog(req.getProviderId(),
                "Review submitted (rating: " + review.getRating() + ")");

        return saved;
    }
}