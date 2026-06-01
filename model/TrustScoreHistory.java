package com.servitrust.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "trust_score_history")
public class TrustScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long providerId;

    @Column(nullable = false)
    private Double delta;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(nullable = false)
    private Double scoreAfter;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public TrustScoreHistory() {}

    public TrustScoreHistory(Long providerId, Double delta, String reason, Double scoreAfter) {
        this.providerId = providerId;
        this.delta = delta;
        this.reason = reason;
        this.scoreAfter = scoreAfter;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public Double getDelta() { return delta; }
    public void setDelta(Double delta) { this.delta = delta; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Double getScoreAfter() { return scoreAfter; }
    public void setScoreAfter(Double scoreAfter) { this.scoreAfter = scoreAfter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}