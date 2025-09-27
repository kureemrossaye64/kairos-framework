// kairos-agentic-framework/src/main/java/com/kairos/agentic_framework/conversational_ingestion/annotations/ConversationalField.java
package com.kairos.agentic_framework.conversational_ingestion.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.kairos.agentic_framework.conversational_ingestion.DefaultFieldProcessor;
import com.kairos.agentic_framework.conversational_ingestion.ExampleProviderStrategy;
import com.kairos.agentic_framework.conversational_ingestion.FieldProcessorStrategy;

/**
 * Marks a field within a @ConversationalEntity as a piece of information
 * to be collected from the user via conversation.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConversationalField {
    /** The question Kaya will ask the user to get this information. */
    String prompt();

    /** An example of a valid answer to help the user. */
    String example() default "";
    
    /**
     * Specifies a custom strategy class for processing and validating this field's input.
     * The class must implement FieldProcessorStrategy and be a Spring bean.
     */
    Class<? extends FieldProcessorStrategy> processor() default DefaultFieldProcessor.class;

    /**
     * Specifies a custom strategy class for providing dynamic examples for this field.
     * The class must implement ExampleProviderStrategy and be a Spring bean.
     */
    Class<? extends ExampleProviderStrategy> exampleProvider() default ExampleProviderStrategy.class;
}