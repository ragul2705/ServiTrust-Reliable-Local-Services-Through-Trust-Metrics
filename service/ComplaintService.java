package com.servitrust.service;

import com.servitrust.dto.ComplaintRequest;
import com.servitrust.dto.ComplaintResponse;

import java.util.List;

public interface ComplaintService {

    ComplaintResponse createComplaint(ComplaintRequest request);

    ComplaintResponse verifyComplaint(Long complaintId);

    ComplaintResponse rejectComplaint(Long complaintId);

    List<ComplaintResponse> getPendingComplaints();
}