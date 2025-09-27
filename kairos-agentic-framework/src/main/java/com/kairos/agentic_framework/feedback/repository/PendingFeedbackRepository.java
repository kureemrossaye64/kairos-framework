package com.kairos.agentic_framework.feedback.repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kairos.agentic_framework.feedback.entity.PendingFeedbackRequest;

public interface PendingFeedbackRepository extends JpaRepository<PendingFeedbackRequest, UUID> {
    List<PendingFeedbackRequest> findByIsDispatchedFalseAndDispatchAtBefore(LocalDateTime now);
    Optional<PendingFeedbackRequest> findByUniqueToken(String token);
}