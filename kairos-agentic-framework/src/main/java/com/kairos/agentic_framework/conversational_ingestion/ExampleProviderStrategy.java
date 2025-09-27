package com.kairos.agentic_framework.conversational_ingestion;

import java.util.List;

/**
 * A strategy interface for dynamically providing example values for a conversational form field.
 */
public interface ExampleProviderStrategy {
    /**
     * @return A list of example valid answers for a field.
     */
    List<String> getExamples();
}