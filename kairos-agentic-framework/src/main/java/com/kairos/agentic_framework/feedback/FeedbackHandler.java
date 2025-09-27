package com.kairos.agentic_framework.feedback;


import java.util.UUID;

import com.kairos.agentic_framework.feedback.entity.PendingFeedbackRequest;

/**
 * A strategy interface for defining how to handle the feedback lifecycle for a specific entity type.
 * @param <T> The specific type of FeedbackTriggerEvent this handler can process.
 */
public interface FeedbackHandler<T extends FeedbackTriggerEvent> {

    /**
     * Returns the user-friendly name of the entity this handler manages (e.g., "Facility").
     * This is used to identify the correct handler.
     */
    String getEntityType();

    /**
     * Checks if this handler is responsible for a given event type.
     * @param event The event that was triggered.
     * @return true if this handler can process the event, false otherwise.
     */
    boolean canHandle(FeedbackTriggerEvent event);

    /**
     * Creates a pending feedback request entity based on the triggered event.
     * @param event The event that occurred.
     * @return A new, unsaved PendingFeedbackRequest entity.
     */
    PendingFeedbackRequest createPendingRequest(T event);

    /**
     * The core logic to process the final user feedback. This is where database
     * updates and vector store ingestion happen.
     * @param entityId The ID of the entity being reviewed.
     * @param rating The user's numerical rating (1-5).
     * @param comment The user's unstructured text comment.
     */
    void processFeedback(UUID entityId, int rating, String comment);
}