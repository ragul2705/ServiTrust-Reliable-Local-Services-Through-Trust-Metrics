package com.servitrust.service;

import com.servitrust.dto.ComplaintRequest;
import com.servitrust.dto.ComplaintResponse;
import com.servitrust.model.Complaint;
import com.servitrust.model.ServiceRequest;
import com.servitrust.repository.ComplaintRepository;
import com.servitrust.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ServiceRequestRepository requestRepository;

    @Autowired
    private ServiceProviderService providerService;

    @Override
    public ComplaintResponse createComplaint(ComplaintRequest request) {

        Long requestId = request.getRequestId();

        // ✅ must exist and be COMPLETED
        ServiceRequest sr = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Service request not found"));

        if (!"COMPLETED".equalsIgnoreCase(sr.getStatus())) {
            throw new RuntimeException("Complaint allowed only after request completion");
        }

        // ✅ only one complaint per request
        if (complaintRepository.existsByRequestId(requestId)) {
            throw new RuntimeException("Complaint already submitted for this request");
        }

        Complaint complaint = new Complaint();
        complaint.setRequestId(requestId);
        complaint.setProviderId(request.getProviderId());
        complaint.setUserId(request.getUserId());
        complaint.setReason(request.getReason() == null ? "No reason" : request.getReason().trim());
        complaint.setDetails(request.getDetails());
        complaint.setStatus("PENDING");

        Complaint saved = complaintRepository.save(complaint);
        return map(saved);
    }

    @Override
    public ComplaintResponse verifyComplaint(Long complaintId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if ("VERIFIED".equalsIgnoreCase(complaint.getStatus())) {
            throw new RuntimeException("Already verified");
        }

        complaint.setStatus("VERIFIED");
        complaint.setVerifiedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        providerService.recomputeAndLog(
                complaint.getProviderId(),
                "Complaint verified (#" + complaintId + ")"
        );

        return map(complaint);
    }

    @Override
    public ComplaintResponse rejectComplaint(Long complaintId) {

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        complaint.setStatus("REJECTED");
        complaintRepository.save(complaint);

        return map(complaint);
    }

    @Override
    public List<ComplaintResponse> getPendingComplaints() {
        return complaintRepository.findByStatus("PENDING")
                .stream()
                .map(this::map)
                .toList();
    }

    private ComplaintResponse map(Complaint c) {
        return new ComplaintResponse(
                c.getId(),
                c.getRequestId(),
                c.getProviderId(),
                c.getUserId(),
                c.getStatus(),
                c.getReason(),
                c.getDetails(),
                c.getCreatedAt() == null ? null : c.getCreatedAt().toString()
        );
    }
}