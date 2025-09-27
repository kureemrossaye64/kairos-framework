package com.kairos.agentic_framework.conversational_ingestion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JPA entity as eligible for conversational ingestion.
 * The 'name' attribute is a user-friendly identifier for the entity type.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConversationalEntity {
    String name();
    String description();
    
    String introMessage() default "";
    
    String outroMessage() default "";
}