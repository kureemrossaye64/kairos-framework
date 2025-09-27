package com.kairos.agentic_framework.transactional_chat.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a "transactional" entity, meaning it can be instantiated
 * through a sophisticated, context-aware conversational flow.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionalEntity {
    /** The user-friendly name of the transaction, e.g., "Booking", "Team Creation". */
    String name();

    /** A brief description of what this transaction accomplishes. */
    String description();
    
    String instructions() default "";
}