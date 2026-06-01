package com.servitrust.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.servitrust.dto.ComplaintResponse;
import com.servitrust.service.ComplaintService;

@RestController
@RequestMapping("/api/admin/complaints")
public class AdminComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/pending")
    public List<ComplaintResponse> pending() {
        return complaintService.getPendingComplaints();
    }

    @PutMapping("/{id}/verify")
    public ComplaintResponse verify(@PathVariable Long id) {
        return complaintService.verifyComplaint(id);
    }

    @PutMapping("/{id}/reject")
    public ComplaintResponse reject(@PathVariable Long id) {
        return complaintService.rejectComplaint(id);
    }
}