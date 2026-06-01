package com.servitrust.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.servitrust.dto.ReviewCreateRequest;
import com.servitrust.model.Review;
import com.servitrust.repository.ReviewRepository;
import com.servitrust.repository.ServiceRequestRepository;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ServiceRequestRepository requestRepository;

    @PostMapping
    public Review createReview(@Valid @RequestBody ReviewCreateRequest body) {

        // 1) prevent duplicates
        if (reviewRepository.existsByRequestId(body.getRequestId())) {
            throw new RuntimeException("Review already submitted for this request");
        }

        // 2) request must exist
        requestRepository.findById(body.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Review r = new Review();
        r.setRequestId(body.getRequestId());
        r.setProviderId(body.getProviderId());
        r.setUserId(body.getUserId());
        r.setRating(body.getRating());
        r.setComment(body.getComment());

        return reviewRepository.save(r);
    }

    @GetMapping("/provider/{providerId}")
    public List<Review> getReviewsByProvider(@PathVariable Long providerId) {
        return reviewRepository.findByProviderId(providerId);
    }
}
