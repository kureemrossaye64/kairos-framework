package com.kairos.agentic_framework.feedback.entity;

import com.kairos.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pending_feedback_requests")
@Getter
@Setter
@NoArgsConstructor
public class PendingFeedbackRequest extends BaseEntity {

    @Column(nullable = false)
    private String entityType; // "Facility", "Partner", "Event"

    @Column(nullable = false)
    private UUID entityId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime dispatchAt;

    @Column(nullable = false)
    private boolean isDispatched = false;

    // A unique, non-guessable token to identify this specific feedback request
    @Column(nullable = false, unique = true)
    private String uniqueToken;
}