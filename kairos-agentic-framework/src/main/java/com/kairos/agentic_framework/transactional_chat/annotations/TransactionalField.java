package com.kairos.agentic_framework.transactional_chat.annotations;

import com.kairos.agentic_framework.conversational_ingestion.FieldProcessorStrategy;
import com.kairos.agentic_framework.conversational_ingestion.DefaultFieldProcessor; // We can reuse this

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field within a @TransactionalEntity as a piece of information
 * that needs to be collected and validated to complete the transaction.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionalField {
    /** A description of what this field represents, for the AI to understand its purpose. */
    String description();

    /**
     * Specifies a custom strategy for processing and validating this field's input.
     * The class must implement FieldProcessorStrategy and be a Spring bean.
     */
    Class<? extends FieldProcessorStrategy> processor() default DefaultFieldProcessor.class;
}