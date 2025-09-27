package com.kairos.agentic_framework.transactional_chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serializable;

@Getter

@JsonInclude(JsonInclude.Include.NON_NULL) // For cleaner JSON sent to the AI
public class TransactionField implements Serializable {
    private final String name;
    private final String description;
    private final String type;
    private boolean isFilled = false;
    private Object value;

    public TransactionField(String name, String description, Class<?> type) {
        this.name = name;
        this.description = description;
        this.type = type.getSimpleName();
    }

    public void setValue(Object value) {
        this.value = value;
        this.isFilled = true;
    }
}