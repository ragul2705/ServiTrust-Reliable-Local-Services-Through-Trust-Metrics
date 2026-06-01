package com.servitrust.dto;

public class ServiceRequestView {
    public Long id;
    public String status;
    public String serviceType;
    public Integer rating;
    public String feedback;
    public boolean complaintSubmitted;

    public ServiceRequestView(Long id, String status, String serviceType,
                              Integer rating, String feedback, boolean complaintSubmitted) {
        this.id = id;
        this.status = status;
        this.serviceType = serviceType;
        this.rating = rating;
        this.feedback = feedback;
        this.complaintSubmitted = complaintSubmitted;
    }
}