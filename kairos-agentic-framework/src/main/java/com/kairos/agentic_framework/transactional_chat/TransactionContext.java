package com.kairos.agentic_framework.transactional_chat;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.kairos.agentic_framework.agent.Context;

@Getter
public class TransactionContext implements Serializable, Context {
    private final String transactionId;
    private final String transactionName;
    private final String transactionDescription;
    private final Map<String, TransactionField> fields;
    private final String aiInstructions;

    public TransactionContext(String transactionName, String transactionDescription, Map<String, TransactionField> fields, String instructions) {
        this.transactionId = UUID.randomUUID().toString();
        this.transactionName = transactionName;
        this.transactionDescription = transactionDescription;
        this.fields = fields;
        this.aiInstructions = instructions;
    }

    public boolean isComplete() {
        return fields.values().stream().allMatch(TransactionField::isFilled);
    }

    @Override
    public String toString() {
        // A user-friendly summary for logging and debugging
        String fieldStatus = fields.entrySet().stream()
                .map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue().isFilled() ? "[FILLED]" : "[PENDING]"))
                .collect(Collectors.joining(", "));
        return String.format("TransactionContext[id=%s, name=%s, complete=%b, fields={%s}]",
                transactionId, transactionName, isComplete(), fieldStatus);
    }
    
    public Object getFieldValue(String fieldName) {
    	return fields.get(fieldName).getValue();
    }

    @Override
	public Map<String, Object> getValues() {
		Map<String,Object> result = new HashMap<String, Object>();
		for(String key : this.fields.keySet()) {
			result.put(key, this.fields.get(key).getValue());
		}
		return result;
	}

	@Override
	public void setFieldValue(String fieldName, Object value) {
		if(!this.fields.containsKey(fieldName)) {
			throw new IllegalArgumentException(fieldName + " does not exist");
		}
		
		this.fields.get(fieldName).setValue(value);
		
	}
}