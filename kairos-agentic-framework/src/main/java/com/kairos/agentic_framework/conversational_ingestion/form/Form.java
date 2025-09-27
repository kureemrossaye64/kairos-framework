package com.kairos.agentic_framework.conversational_ingestion.form;

import com.kairos.agentic_framework.agent.Context;
import com.kairos.agentic_framework.conversational_ingestion.annotations.ConversationalEntity;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class Form implements Context{

    private final Class<?> entityClass;
    private final String entityName;
    private final List<FormField> fields;
    private final Map<String, FormField> fieldMap;
    private FormState state;

    public Form(Class<?> entityClass, List<FormField> fields) {
        this.entityClass = entityClass;
        this.fields = fields;
        this.entityName = entityClass.getAnnotation(ConversationalEntity.class).name();
        this.fieldMap = fields.stream()
                .collect(Collectors.toMap(FormField::getName, Function.identity()));
    }

    /**
     * Finds the next field that has not yet been filled by the user.
     * @return An Optional containing the next FormField, or an empty Optional if the form is complete.
     */
    public Optional<FormField> getNextUnfilledField() {
        return fields.stream().filter(f -> f.getValue() == null || f.getValue().toString().isBlank()).findFirst();
    }

    /**
     * Checks if all fields in the form have been filled.
     * @return true if the form is complete, false otherwise.
     */
    public boolean isComplete() {
        return getNextUnfilledField().isEmpty();
    }

    /**
     * Retrieves the value of a specific field by its name.
     * @param fieldName The name of the field (must match the Java field name in the entity).
     * @return The user-provided value for the field.
     * @throws IllegalArgumentException if the field does not exist.
     */
    public Object getFieldValue(String fieldName) {
        if (!fieldMap.containsKey(fieldName)) {
            throw new IllegalArgumentException("Form for entity '" + entityName + "' does not contain a field named '" + fieldName + "'");
        }
        return fieldMap.get(fieldName).getValue();
    }
    
    public String getString(String fieldName) {
    	Object val = getFieldValue(fieldName);
    	if(val instanceof String) {
    		return (String)val;
    	}else {
    		return val == null? "": val.toString();
    	}
    }

	public void setState(FormState state) {
		this.state = state;
	}
    
	/**
     * Generates a human-readable summary of all collected data for confirmation.
     * @return A formatted string summary.
     */
    public String generateSummary() {
        StringBuilder summary = new StringBuilder("Please confirm the following information for your " + this.entityName + ":\n");
        for (FormField field : fields) {
            summary.append(String.format("- **%s**: %s\n", field.getName(), field.getValue()));
        }
        summary.append("\nIs this correct? (yes/no)");
        return summary.toString();
    }

	public Object getFieldValueAsObject(String fieldName) {
		return getFieldValue(fieldName);
	}

	@Override
	public Map<String, Object> getValues() {
		Map<String,Object> result = new HashMap<String, Object>();
		for(String key : this.fieldMap.keySet()) {
			result.put(key, this.fieldMap.get(key).getValue());
		}
		return result;
	}

	@Override
	public void setFieldValue(String fieldName, Object value) {
		if(!this.fieldMap.containsKey(fieldName)) {
			throw new IllegalArgumentException(fieldName + " does not exist");
		}
		
		this.fieldMap.get(fieldName).setValue(value);
		
	}
}