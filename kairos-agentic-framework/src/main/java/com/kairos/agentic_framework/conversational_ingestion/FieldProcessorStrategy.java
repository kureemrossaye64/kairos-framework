package com.kairos.agentic_framework.conversational_ingestion;

/**
 * A strategy interface for processing and validating the raw text input for a specific entity field.
 * Implementations of this can perform database lookups, fuzzy matching, or complex transformations.
 */
public interface FieldProcessorStrategy {

    /**
     * Processes the user's raw text input.
     * @param rawInput The text provided by the user.
     * @return A ProcessingResult containing either the successfully processed object or an error message.
     */
    ProcessingResult process(String rawInput);

    /**
     * A record to encapsulate the result of a processing attempt.
     * @param isSuccess True if the input was successfully processed and validated.
     * @param processedValue The resulting object (e.g., a Facility entity) if successful. Can be null on failure.
     * @param guidanceMessage A user-facing message, either an error or a clarification prompt.
     */
    record ProcessingResult(boolean isSuccess, Object processedValue, String guidanceMessage) {
        public static ProcessingResult success(Object value) {
            return new ProcessingResult(true, value, null);
        }
        public static ProcessingResult failure(String guidance) {
            return new ProcessingResult(false, null, guidance);
        }
    }
}