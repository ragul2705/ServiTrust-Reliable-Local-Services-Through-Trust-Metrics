package com.servitrust.dto;

import java.util.Map;

public class TrustScoreResponse {
    private Long providerId;
    private double trustScore;
    private String level;

    private double completionRate;
    private double averageRating;
    private int completedJobs;
    private int totalRequests;
    private double availabilityScore;
    private double penalty;

    private Map<String, Double> breakdown;

    public TrustScoreResponse(Long providerId, double trustScore, String level,
                              double completionRate, double averageRating, int completedJobs,
                              int totalRequests, double availabilityScore, double penalty,
                              Map<String, Double> breakdown) {
        this.providerId = providerId;
        this.trustScore = trustScore;
        this.level = level;
        this.completionRate = completionRate;
        this.averageRating = averageRating;
        this.completedJobs = completedJobs;
        this.totalRequests = totalRequests;
        this.availabilityScore = availabilityScore;
        this.penalty = penalty;
        this.breakdown = breakdown;
    }

    public Long getProviderId() { return providerId; }
    public double getTrustScore() { return trustScore; }
    public String getLevel() { return level; }
    public double getCompletionRate() { return completionRate; }
    public double getAverageRating() { return averageRating; }
    public int getCompletedJobs() { return completedJobs; }
    public int getTotalRequests() { return totalRequests; }
    public double getAvailabilityScore() { return availabilityScore; }
    public double getPenalty() { return penalty; }
    public Map<String, Double> getBreakdown() { return breakdown; }
}
