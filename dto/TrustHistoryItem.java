package com.servitrust.dto;

public class TrustHistoryItem {
    private Long id;
    private Long providerId;
    private Double delta;
    private String reason;
    private Double scoreAfter;
    private String createdAt;

    public TrustHistoryItem(Long id, Long providerId, Double delta, String reason, Double scoreAfter, String createdAt) {
        this.id = id;
        this.providerId = providerId;
        this.delta = delta;
        this.reason = reason;
        this.scoreAfter = scoreAfter;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getProviderId() { return providerId; }
    public Double getDelta() { return delta; }
    public String getReason() { return reason; }
    public Double getScoreAfter() { return scoreAfter; }
    public String getCreatedAt() { return createdAt; }
}