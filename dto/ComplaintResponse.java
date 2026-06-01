package com.servitrust.dto;

public class ComplaintResponse {

    private Long id;
    private Long requestId;
    private Long providerId;
    private Long userId;
    private String status;
    private String reason;
    private String details;
    private String createdAt;

    public ComplaintResponse(Long id, Long requestId, Long providerId, Long userId, String status, String reason, String details, String createdAt) {
        this.id = id;
        this.requestId = requestId;
        this.providerId = providerId;
        this.userId = userId;
        this.status = status;
        this.reason = reason;
        this.details = details;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getRequestId() { return requestId; }
    public Long getProviderId() { return providerId; }
    public Long getUserId() { return userId; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public String getDetails() { return details; }
    public String getCreatedAt() { return createdAt; }
}