package com.kairos.agentic_framework.transactional_chat;

import com.kairos.agentic_framework.agent.ContextAwareStrategy;
import com.kairos.agentic_framework.conversational_ingestion.FieldProcessorStrategy;
import com.kairos.agentic_framework.transactional_chat.annotations.TransactionalField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService {

    private final TransactionDefinitionProvider definitionProvider;
    private final ApplicationContext context; // To get strategy beans by class
    private final Map<String, TransactionCompletionStrategy> completionStrategyMap;

    // In-memory store for active transactions. Key: conversationId
    private final Map<String, TransactionContext> activeTransactions = new ConcurrentHashMap<>();

    public TransactionService(TransactionDefinitionProvider definitionProvider, ApplicationContext context, List<TransactionCompletionStrategy> strategies) {
        this.definitionProvider = definitionProvider;
        this.context = context;
        this.completionStrategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getTransactionName().toLowerCase(),
                        Function.identity()
                ));
        log.info("Initialized TransactionService with completion strategies for: {}", completionStrategyMap.keySet());
    }

    public TransactionContext startTransaction(String conversationId, String transactionName) {
        if (!completionStrategyMap.containsKey(transactionName.toLowerCase())) {
            throw new IllegalArgumentException("No completion strategy found for transaction: " + transactionName);
        }
        log.info("Starting new transaction '{}' for conversation ID: {}", transactionName, conversationId);
        TransactionContext newContext = definitionProvider.buildContext(transactionName);
        activeTransactions.put(conversationId, newContext);
        return newContext;
    }

    public FieldProcessorStrategy.ProcessingResult validateAndSetField(String conversationId, String fieldName, String userValue) {
        TransactionContext context = getActiveTransaction(conversationId);
        TransactionField field = context.getFields().get(fieldName);

        if (field == null) {
            return FieldProcessorStrategy.ProcessingResult.failure("Invalid field '" + fieldName + "' for this transaction.");
        }
        if (field.isFilled()) {
            return FieldProcessorStrategy.ProcessingResult.failure("The field '" + fieldName + "' has already been filled.");
        }

        try {
            // Use reflection to get the processor class from the original entity's annotation
            Class<?> entityClass = definitionProvider.getTransactionEntityClass(context.getTransactionName());
            Field declaredField = entityClass.getDeclaredField(fieldName);
            TransactionalField annotation = declaredField.getAnnotation(TransactionalField.class);

            // Get the processor bean from the application context
            FieldProcessorStrategy processor = this.context.getBean(annotation.processor());
            
            if(processor instanceof ContextAwareStrategy) {
				((ContextAwareStrategy)processor).setContext(context);
			}
            
            // Execute the specific validation and processing logic
            FieldProcessorStrategy.ProcessingResult result = processor.process(userValue);
            
            if (result.isSuccess()) {
                field.setValue(result.processedValue());
                log.info("Successfully validated and set field '{}' for conversation ID: {}", fieldName, conversationId);
            }
            return result;
        } catch (NoSuchFieldException e) {
            log.error("Mismatch between TransactionContext and @TransactionalEntity for field '{}'", fieldName, e);
            return FieldProcessorStrategy.ProcessingResult.failure("Internal configuration error for field: " + fieldName);
        }
    }

    public TransactionContext getTransactionStatus(String conversationId) {
        return getActiveTransaction(conversationId);
    }

    public String cancelTransaction(String conversationId) {
    	 activeTransactions.remove(conversationId); // Clean up on failure
    	 return "I apologies for inconvenience"; 
    }
    
    public String commitTransaction(String conversationId) {
        TransactionContext context = getActiveTransaction(conversationId);
        if (!context.isComplete()) {
            log.warn("Attempted to commit an incomplete transaction for conversation ID: {}", conversationId);
            return "I'm sorry, I can't proceed yet as some information is still missing. Let's continue.";
        }

        TransactionCompletionStrategy strategy = completionStrategyMap.get(context.getTransactionName().toLowerCase());
        log.info("Committing transaction '{}' for conversation ID: {}", context.getTransactionName(), conversationId);
        
        try {
            String resultMessage = strategy.execute(context);
            activeTransactions.remove(conversationId); // Clean up on success
            return resultMessage;
        } catch (Exception e) {
            log.error("Error executing completion strategy for transaction {}:", context.getTransactionName(), e);
           
            activeTransactions.remove(conversationId); // Clean up on failure
            return "I'm sorry, an unexpected error occurred while finalizing your request. Please try again later.";
        }
    }
    
    private TransactionContext getActiveTransaction(String conversationId) {
        TransactionContext context = activeTransactions.get(conversationId);
        if (context == null) {
            throw new IllegalStateException("No active transaction found for this conversation.");
        }
        return context;
    }
}