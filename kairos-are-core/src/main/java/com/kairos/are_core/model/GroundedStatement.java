package com.kairos.are_core.model;

import java.io.Serializable;
import java.util.List;

/**
 * The core data structure for all information within the ARE.
 * This object ensures all data is traceable and has a clear confidence level.
 * Implements Serializable to be used as a value in our FormField.
 */


public record GroundedStatement(
    String proposition,
    String source, // e.g., "PostgreSQL:facilities:uuid", "ChromaDB:doc_id", "UserSubmission:username"
    Confidence confidence,
    List<String> trace // A log of how this statement was derived
) implements Serializable { // Implement Serializable

    public enum Confidence {
        AXIOMATIC,   // Derived directly from the Axiomatic Set Ω.
        VERIFIED,    // A provisional statement that has passed the Guardian check.
        PROVISIONAL  // Raw, unverified data from an external source (tool).
    }
}