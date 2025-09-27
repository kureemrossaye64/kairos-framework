package com.kairos.agentic_framework.feedback;

import java.util.UUID;

/**
 * A marker interface for events that should trigger a feedback request.
 * Any class implementing this can be used to initiate the feedback loop.
 */
public interface FeedbackTriggerEvent {
    /** The unique ID of the entity the feedback is about (e.g., Facility ID, Partner ID). */
    UUID getEntityId();

    /** The unique ID of the user who should receive the feedback request. */
    UUID getUserId();
}