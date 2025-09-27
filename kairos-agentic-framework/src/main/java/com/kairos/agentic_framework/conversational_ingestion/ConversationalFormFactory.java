package com.kairos.agentic_framework.conversational_ingestion;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import com.kairos.agentic_framework.conversational_ingestion.annotations.ConversationalEntity;
import com.kairos.agentic_framework.conversational_ingestion.annotations.ConversationalField;
import com.kairos.agentic_framework.conversational_ingestion.form.Form;
import com.kairos.agentic_framework.conversational_ingestion.form.FormField;
import com.kairos.agentic_framework.conversational_ingestion.form.FormState;

@Component
public class ConversationalFormFactory {

    private final Map<String, Class<?>> entityMap;

    public ConversationalFormFactory() {
        // On startup, scan the entire project for classes annotated with @ConversationalEntity
        Reflections reflections = new Reflections("com.kairos");
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(ConversationalEntity.class);
        this.entityMap = entities.stream()
            .collect(Collectors.toMap(
                cls -> cls.getAnnotation(ConversationalEntity.class).name().toLowerCase(),
                Function.identity()
            ));
    }
    
    public String getOutroMessage(String entityName) {
    	Class<?> entityClass = entityMap.get(entityName.toLowerCase());
        if (entityClass == null) {
            throw new IllegalArgumentException("No conversational entity found with name: " + entityName);
        }
        
        
        ConversationalEntity conv = entityClass.getAnnotation(ConversationalEntity.class);
        
        String outro = conv.outroMessage();
        if(outro == null || outro.trim().length() == 0) {
        	
        	outro = "Excellent! I have saved your information. Thank you for contributing!";
        	//return String.format(intro,entityName);
        	
        	
        }
        return outro;
    }
    
    public String getIntroMessage(String entityName) {
    	Class<?> entityClass = entityMap.get(entityName.toLowerCase());
        if (entityClass == null) {
            throw new IllegalArgumentException("No conversational entity found with name: " + entityName);
        }
        
        
        ConversationalEntity conv = entityClass.getAnnotation(ConversationalEntity.class);
        
        String intro = conv.introMessage();
        if(intro == null || intro.trim().length() == 0) {
        	
        	intro = "I can help with that by registering your '%s' on our platform. " +
                    "This will make your service discoverable to the entire community when they search for activities like yours. " +
                    "The process involves me asking a few simple questions to gather the necessary details. " +
                    "Would you like to proceed with the registration?";
        	return String.format(intro,entityName);
        	
        	
        }
        return intro;
    }

    public Form createForm(String entityName) {
        Class<?> entityClass = entityMap.get(entityName.toLowerCase());
        if (entityClass == null) {
            throw new IllegalArgumentException("No conversational entity found with name: " + entityName);
        }

        List<FormField> fields = Arrays.stream(entityClass.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(ConversationalField.class))
            .map(this::createFormField)
            .collect(Collectors.toList());

        Form form = new Form(entityClass, fields);
        form.setState(FormState.COLLECTING_ANSWERS);
        return form;
    }

    private FormField createFormField(Field field) {
        ConversationalField annotation = field.getAnnotation(ConversationalField.class);
        return new FormField(field.getName(), annotation.prompt(), annotation.example(), annotation.processor());
    }
}