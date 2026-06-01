package com.servitrust.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.servitrust.dto.TrustHistoryItem;
import com.servitrust.dto.TrustScoreResponse;
import com.servitrust.exception.UserNotFoundException;
import com.servitrust.model.Review;
import com.servitrust.model.ServiceProvider;
import com.servitrust.model.ServiceRequest;
import com.servitrust.model.TrustScoreHistory;
import com.servitrust.repository.ComplaintRepository;
import com.servitrust.repository.ReviewRepository;
import com.servitrust.repository.ServiceProviderRepository;
import com.servitrust.repository.ServiceRequestRepository;
import com.servitrust.repository.TrustScoreHistoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceProviderServiceImpl implements ServiceProviderService {

    @Autowired private ServiceProviderRepository providerRepository;
    @Autowired private ServiceRequestRepository requestRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private TrustScoreHistoryRepository trustHistoryRepository;
    @Autowired private ComplaintRepository complaintRepository;

    private static final double BASE_SCORE = 50.0;

    @Override
    public ServiceProvider createProvider(ServiceProvider provider) {
        if (provider.getTrustScore() == null || provider.getTrustScore() <= 0) {
            provider.setTrustScore(BASE_SCORE);
        }
        return providerRepository.save(provider);
    }

    @Override
    public ServiceProvider getProviderById(Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Provider not found with id: " + id));
    }

    @Override
    public List<ServiceProvider> getAllProviders() {
        return providerRepository.findAll();
    }

    @Override
    public TrustScoreResponse getTrustScoreDetails(Long providerId) {
        ServiceProvider provider = getProviderById(providerId);

        List<ServiceRequest> requests = requestRepository.findByProviderId(providerId);

        long completedCount = requests.stream()
                .filter(r -> "COMPLETED".equalsIgnoreCase(safeStatus(r)))
                .count();

        long cancelledByProvider = requests.stream()
                .filter(r -> "CANCELLED_BY_PROVIDER".equalsIgnoreCase(safeStatus(r)))
                .count();

        long cancelledByUser = requests.stream()
                .filter(r -> "CANCELLED_BY_USER".equalsIgnoreCase(safeStatus(r)))
                .count();

        // ✅ Only consider finished outcomes (ignore PENDING/ACCEPTED)
        long finishedRequests = completedCount + cancelledByProvider + cancelledByUser;

        double completionRate = (finishedRequests == 0)
                ? 0
                : (completedCount * 100.0 / finishedRequests);

        // Avg rating
        List<Review> reviews = reviewRepository.findByProviderId(providerId);
        double avgRating;
        if (reviews.isEmpty()) {
            avgRating = provider.getRating() == null ? 0.0 : provider.getRating();
        } else {
            avgRating = reviews.stream().mapToDouble(Review::getRating).average().orElse(0);
        }

        int completedJobs = provider.getCompletedJobs() == null ? 0 : provider.getCompletedJobs();

        boolean isAvailable =
                provider.getAvailabilityStatus() != null
                        && "AVAILABLE".equalsIgnoreCase(provider.getAvailabilityStatus());

        long verifiedComplaints = complaintRepository.countByProviderIdAndStatus(providerId, "VERIFIED");

        // =========================
        // ✅ RULE-BASED TRUST SCORE
        // =========================

        double ratingPoints = ratingToPoints(avgRating);               // 1★ -> -5
        double completionPoints = Math.min(completedJobs * 0.5, 15.0);  // small growth
        double availabilityPoints = isAvailable ? 2.0 : 0.0;

        double cancelPenalty = (cancelledByProvider * 4.0) + (cancelledByUser * 1.0);
        double complaintPenalty = verifiedComplaints * 5.0;

        double rawScore = BASE_SCORE
                + ratingPoints
                + completionPoints
                + availabilityPoints
                - cancelPenalty
                - complaintPenalty;

        double score = clamp(round2(rawScore), 0, 100);

        String level;
        if (score >= 85) level = "A (Highly Trusted)";
        else if (score >= 70) level = "B (Trusted)";
        else if (score >= 55) level = "C (Moderate)";
        else level = "D (Low Trust)";

        Map<String, Double> breakdown = new HashMap<>();
        breakdown.put("baseScore", BASE_SCORE);
        breakdown.put("ratingPoints", round2(ratingPoints));
        breakdown.put("completionPoints", round2(completionPoints));
        breakdown.put("availabilityPoints", round2(availabilityPoints));
        breakdown.put("complaintPenalty", -round2(complaintPenalty));
        breakdown.put("cancelPenalty", -round2(cancelPenalty));

        provider.setTrustScore(score);
        providerRepository.save(provider);

        double penaltyTotal = cancelPenalty + complaintPenalty;

        return new TrustScoreResponse(
                providerId,
                score,
                level,
                round2(completionRate),
                round2(avgRating),
                completedJobs,
                (int) finishedRequests,  // finished only
                isAvailable ? 100 : 50,
                round2(penaltyTotal),
                breakdown
        );
    }

    @Override
    public List<TrustScoreResponse> getProviderRanking() {
        return providerRepository.findAll().stream()
                .map(p -> getTrustScoreDetails(p.getId()))
                .sorted(Comparator.comparingDouble(TrustScoreResponse::getTrustScore).reversed())
                .toList();
    }

    @Override
    public void recomputeAndLog(Long providerId, String reason) {

        double before = providerRepository.findById(providerId)
                .map(p -> p.getTrustScore() == null ? BASE_SCORE : p.getTrustScore())
                .orElse(BASE_SCORE);

        TrustScoreResponse afterResp = getTrustScoreDetails(providerId);
        double afterScore = afterResp.getTrustScore();

        double delta = round2(afterScore - before);

        // ✅ DO NOT store 0.00 history entries
        if (Math.abs(delta) < 0.0001) return;

        trustHistoryRepository.save(new TrustScoreHistory(providerId, delta, reason, afterScore));
    }
    @Override
    public void applyVerifiedComplaint(Long providerId, Long complaintId) {
        recomputeAndLog(providerId, "Complaint verified (#" + complaintId + ")");
    }

    @Override
    public List<TrustHistoryItem> getTrustHistory(Long providerId) {
        return trustHistoryRepository.findByProviderIdOrderByCreatedAtDesc(providerId).stream()
                .map(h -> new TrustHistoryItem(
                        h.getId(),
                        h.getProviderId(),
                        h.getDelta(),
                        h.getReason(),
                        h.getScoreAfter(),
                        h.getCreatedAt().toString()
                ))
                .toList();
    }

    private double ratingToPoints(double avgRating) {
        if (avgRating <= 0) return 0.0;
        double points = (avgRating - 3.0)*4.0;

        return clamp(points, -10.0, 10.0);
    }

    private String safeStatus(ServiceRequest r) {
        try {
            Object s = r.getStatus();
            return (s == null) ? "" : s.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}