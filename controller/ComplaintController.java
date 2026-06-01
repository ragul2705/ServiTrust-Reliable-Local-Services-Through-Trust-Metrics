package com.servitrust.controller;

import com.servitrust.dto.ComplaintRequest;
import com.servitrust.dto.ComplaintResponse;
import com.servitrust.service.ComplaintService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @PostMapping
    public ComplaintResponse create(@Valid @RequestBody ComplaintRequest request) {
        return complaintService.createComplaint(request);
    }

    @PutMapping("/{id}/verify")
    public ComplaintResponse verify(@PathVariable Long id) {
        return complaintService.verifyComplaint(id);
    }

    @PutMapping("/{id}/reject")
    public ComplaintResponse reject(@PathVariable Long id) {
        return complaintService.rejectComplaint(id);
    }

    @GetMapping("/pending")
    public List<ComplaintResponse> pending() {
        return complaintService.getPendingComplaints();
    }
}