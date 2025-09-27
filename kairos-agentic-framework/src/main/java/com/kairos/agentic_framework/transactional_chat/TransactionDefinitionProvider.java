package com.kairos.agentic_framework.transactional_chat;

import com.kairos.agentic_framework.transactional_chat.annotations.TransactionalEntity;
import com.kairos.agentic_framework.transactional_chat.annotations.TransactionalField;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TransactionDefinitionProvider {

    private final Map<String, Class<?>> transactionalEntityMap;

    public TransactionDefinitionProvider() {
        Reflections reflections = new Reflections("com.kairos");
        Set<Class<?>> entities = reflections.getTypesAnnotatedWith(TransactionalEntity.class);
        this.transactionalEntityMap = entities.stream()
                .collect(Collectors.toMap(
                        cls -> cls.getAnnotation(TransactionalEntity.class).name().toLowerCase(),
                        Function.identity()
                ));
        log.info("Discovered @TransactionalEntity classes: {}", transactionalEntityMap.keySet());
    }
    
    public Class<?> getTransactionEntityClass(String transactionName){
    	Class<?> entityClass = transactionalEntityMap.get(transactionName.toLowerCase());
    	if (entityClass == null) {
            throw new IllegalArgumentException("No @TransactionalEntity found with name: " + transactionName);
        }
    	return entityClass;
    }

    public TransactionContext buildContext(String transactionName) {
        Class<?> entityClass = transactionalEntityMap.get(transactionName.toLowerCase());
        if (entityClass == null) {
            throw new IllegalArgumentException("No @TransactionalEntity found with name: " + transactionName);
        }

        TransactionalEntity entityAnnotation = entityClass.getAnnotation(TransactionalEntity.class);

        Map<String, TransactionField> fields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(TransactionalField.class))
                .map(this::createTransactionField)
                .collect(Collectors.toMap(TransactionField::getName, Function.identity()));

        return new TransactionContext(entityAnnotation.name(), entityAnnotation.description(), fields, entityAnnotation.instructions());
    }

    private TransactionField createTransactionField(Field field) {
        TransactionalField annotation = field.getAnnotation(TransactionalField.class);
        return new TransactionField(field.getName(), annotation.description(), field.getType());
    }
}