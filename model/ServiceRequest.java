package com.servitrust.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "service_requests")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @JsonProperty("userId")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @JsonProperty("providerId")
    @Column(name = "provider_id", nullable = false)
    private Long providerId;

    @JsonProperty("userName")
    @Column(name = "user_name", length = 120)
    private String userName;

    @NotBlank
    @JsonProperty("serviceType")
    @Column(name = "service_type", nullable = false, length = 80)
    private String serviceType;

    @NotBlank
    @JsonProperty("status")
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @JsonProperty("description")
    @Column(name = "description", length = 500)
    private String description;

    @JsonProperty("address")
    @Column(name = "address", length = 255)
    private String address;

    @JsonProperty("rating")
    @Column(name = "rating")
    private Integer rating;

    @JsonProperty("feedback")
    @Column(name = "feedback", length = 1000)
    private String feedback;

    @JsonProperty("reviewed")
    @Column(name = "reviewed", nullable = false)
    private Boolean reviewed = false;

    @JsonProperty("complaintRaised")
    @Column(name = "complaint_raised", nullable = false)
    private Boolean complaintRaised = false;

    public Boolean getComplaintRaised() { return complaintRaised; }
    public void setComplaintRaised(Boolean complaintRaised) { this.complaintRaised = complaintRaised; }

    public ServiceRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Boolean getReviewed() { return reviewed; }
    public void setReviewed(Boolean reviewed) { this.reviewed = reviewed; }
}