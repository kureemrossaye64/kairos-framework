package com.kairos.agentic_framework.conversational_ingestion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.kairos.agentic_framework.agent.ContextAwareStrategy;
import com.kairos.agentic_framework.conversational_ingestion.form.Form;
import com.kairos.agentic_framework.conversational_ingestion.form.FormField;
import com.kairos.agentic_framework.conversational_ingestion.form.FormState;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GenericIngestionService {

    private final ConversationalFormFactory formFactory;
    private final Map<String, FormCompletionStrategy> completionStrategyMap;
    public static final String INGESTION_SIGNAL = "KAYA_INGESTION_FLOW::";
    private final Validator validator;

    private final Map<String, Form> activeForms = new ConcurrentHashMap<>();
    
    private final ApplicationContext context;

    // Spring will automatically inject a list of all beans that implement the strategy interface.
    public GenericIngestionService(ConversationalFormFactory formFactory, List<FormCompletionStrategy> strategies, Validator validator, ApplicationContext context) {
        this.formFactory = formFactory;
        this.context = context;
        this.completionStrategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getEntityName().toLowerCase(),
                        Function.identity()
                ));
        this.validator = validator;
        log.info("Initialized GenericIngestionService with strategies for: {}", completionStrategyMap.keySet());
    }

    public String startIngestion(String conversationId, String entityName, boolean dryRun) {
        if (!completionStrategyMap.containsKey(entityName.toLowerCase())) {
            return "I'm sorry, I don't know how to register a '" + entityName + "'. I can help with: " + String.join(", ", completionStrategyMap.keySet());
        }
        if (dryRun) {
            // DRY RUN: Return a proposal instead of starting the form.
            log.info("Executing dry run for ingestion of '{}'", entityName);
            return formFactory.getIntroMessage(entityName);
        } else {
	        log.info("Starting new conversational ingestion for '{}'. Conversation ID: {}", entityName, conversationId);
	        Form form = formFactory.createForm(entityName);
	        activeForms.put(conversationId, form);
	        return "KAYA_INGESTION_FLOW::" +form.getNextUnfilledField()
	                .map(FormField::getPrompt)
	                .orElse("It seems this form has no fields to fill. Process complete.");
        }
    }

    public String processAnswer(String conversationId, String answer) {
        Form form = activeForms.get(conversationId);
        if (form == null || form.getState() != FormState.COLLECTING_ANSWERS) {
            return "We are not currently in the process of gathering information. How can I help you start?";
        }

        FormField currentField = form.getNextUnfilledField().orElse(null);
        if (currentField == null) return "It looks like we have all the information already.";
        try {
            // Use reflection to validate the answer against the constraints on the target entity's field
            BeanWrapper beanWrapper = new BeanWrapperImpl(form.getEntityClass().getDeclaredConstructor().newInstance());
            beanWrapper.setPropertyValue(currentField.getName(), answer); // Temporarily set the value
            Set<ConstraintViolation<Object>> violations = validator.validateProperty(beanWrapper.getWrappedInstance(), currentField.getName());

            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                log.warn("Validation failed for field '{}' with value '{}'. Reason: {}", currentField.getName(), answer, errorMessage);
                return "There was an issue with your answer: " + errorMessage + ". Please try again.\n\n" + currentField.getPrompt();
            }
        } catch (Exception e) {
            log.warn("Could not perform validation for field '{}'. Accepting value. Error: {}", currentField.getName(), e.getMessage());
        }
        
        FieldProcessorStrategy processor = context.getBean(currentField.getProcessorClass());
        if(processor instanceof ContextAwareStrategy) {
			((ContextAwareStrategy)processor).setContext(form);
		}
        FieldProcessorStrategy.ProcessingResult result = processor.process(answer);
        if (!result.isSuccess()) {
            // VALIDATION FAILED or requires clarification
            log.warn("Field processor for '{}' rejected input '{}'. Guidance: {}", currentField.getName(), answer, result.guidanceMessage());
            return result.guidanceMessage() + "\n\nLet's try again: " + currentField.getPrompt();
        }
        currentField.setValue(result.processedValue());
        log.debug("Processed answer '{}' for field '{}' in conversation {}", answer, currentField.getName(), conversationId);

        Optional<FormField> nextField = form.getNextUnfilledField();
        if (nextField.isPresent()) {
            // THE FIX: Prepend the signal to the next question.
            return INGESTION_SIGNAL + nextField.get().getPrompt();
        } else {
           
        	form.setState(FormState.PENDING_CONFIRMATION);
            log.info("Form for '{}' is complete. Moving to PENDING_CONFIRMATION for conversation ID: {}", form.getEntityName(), conversationId);
            return form.generateSummary(); // Return the summary for user confirmation
        }
    }
    
    
    private String validate(FormField currentField, Form form, String answer) {
    	try {
            // Use reflection to validate the answer against the constraints on the target entity's field
            BeanWrapper beanWrapper = new BeanWrapperImpl(form.getEntityClass().getDeclaredConstructor().newInstance());
            beanWrapper.setPropertyValue(currentField.getName(), answer); // Temporarily set the value
            Set<ConstraintViolation<Object>> violations = validator.validateProperty(beanWrapper.getWrappedInstance(), currentField.getName());

            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                log.warn("Validation failed for field '{}' with value '{}'. Reason: {}", currentField.getName(), answer, errorMessage);
                return "There was an issue with your answer: " + errorMessage + ". Please try again.\n\n" + currentField.getPrompt();
            }
        } catch (Exception e) {
            log.warn("Could not perform validation for field '{}'. Accepting value. Error: {}", currentField.getName(), e.getMessage());
            return null;
        }
    	return null;
    }
    
    
    public String correctAnswer(String conversationId, String answer, String previousAnswer) {
        Form form = activeForms.get(conversationId);
        if (form == null || form.getState() != FormState.PENDING_CONFIRMATION) {
            return "We are not currently in the process of gathering information. How can I help you start?";
        }
        
        for(FormField field : form.getFields()) {
        	if(field.getValue().equals(previousAnswer)) {
        		String validate = validate(field, form, answer);
        		if(validate == null) {
        			FieldProcessorStrategy processor = context.getBean(field.getProcessorClass());
        			if(processor instanceof ContextAwareStrategy) {
        				((ContextAwareStrategy)processor).setContext(form);
        			}
        	        FieldProcessorStrategy.ProcessingResult result = processor.process(answer);
        	        if (!result.isSuccess()) {
        	            // VALIDATION FAILED or requires clarification
        	            log.warn("Field processor for '{}' rejected input '{}'. Guidance: {}", field.getName(), answer, result.guidanceMessage());
        	            return result.guidanceMessage() + "\n\nLet's try again: " + field.getPrompt();
        	        }
        			field.setValue(result.processedValue());
        			return form.generateSummary();
        		}else {
        			return validate;
        		}
        	}
        }
        
        return "It seems that I cannot update the previous answer you provided. Can you tell me again which answer was incorrect";
    }
    
    public String confirmAndSave(String conversationId) {
        Form form = activeForms.get(conversationId);
        if (form == null || form.getState() != FormState.PENDING_CONFIRMATION) {
            return "There is nothing pending confirmation right now.";
        }
        
        // ... (Execute strategy, handle errors, as before) ...
        FormCompletionStrategy strategy = completionStrategyMap.get(form.getEntityName().toLowerCase());
        try {
            strategy.execute(form);
            activeForms.remove(conversationId); // Clean up
            return formFactory.getOutroMessage(form.getEntityName());
        } catch (Exception e) {
            activeForms.remove(conversationId);
            return "I'm sorry, an unexpected error occurred while saving. Please try the registration process again.";
        }
    }
}