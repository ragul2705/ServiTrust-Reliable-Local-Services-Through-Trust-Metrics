package com.servitrust.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.servitrust.model.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByStatus(String status);

    long countByProviderIdAndStatus(Long providerId, String status);

    boolean existsByRequestId(Long requestId);

    Complaint findTopByRequestIdOrderByCreatedAtDesc(Long requestId);
}