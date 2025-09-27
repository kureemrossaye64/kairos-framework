package com.kairos.agentic_framework.conversational_ingestion.form;

import com.kairos.agentic_framework.conversational_ingestion.FieldProcessorStrategy;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class FormField {
    private final String name;
    private final String prompt;
    private final String example;
    @Setter
    private Object value; // This holds the user's answer
    private final Class<? extends FieldProcessorStrategy> processorClass;
}