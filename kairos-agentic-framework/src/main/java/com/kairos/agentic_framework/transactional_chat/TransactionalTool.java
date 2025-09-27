package com.kairos.agentic_framework.transactional_chat;

import com.kairos.agentic_framework.conversational_ingestion.FieldProcessorStrategy.ProcessingResult;
import com.kairos.agentic_framework.tools.KairosTool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A suite of tools for managing complex, stateful, context-aware conversational transactions.
 * This is a generic tool that can handle any process annotated with @TransactionalEntity.
 */
@KairosTool
@RequiredArgsConstructor
public class TransactionalTool {

    private final TransactionService transactionService;

    
    //@Tool("Starts a complex, multi-step conversational process, such as booking a facility. This provides the AI with the full context of information needed to complete the task.")
    public TransactionContext startTransaction(
            @P("The name of the transaction to start") String transactionName
    ) {
        return transactionService.startTransaction(getConversationId(), transactionName);
    }

    @Tool("Validates and sets a single piece of information provided by the user for the current ongoing transaction. This should be called for each piece of data collected.")
    public ProcessingResult validateAndSetField(
            @P("The name of the field to set, which must match a field in the TransactionContext, e.g., 'facility', 'startTime'.") String fieldName,
            @P("The value for the field as provided by the user in natural language.") String userValue
    ) {
        return transactionService.validateAndSetField(getConversationId(), fieldName, userValue);
    }

    @Tool("Retrieves the current status of the ongoing transaction, showing which fields have been filled and which are still pending.")
    public TransactionContext checkTransactionStatus() {
        return transactionService.getTransactionStatus(getConversationId());
    }

    @Tool("Finalizes and commits the transaction after all required information has been collected and validated. This should only be called when the transaction is complete.")
    public String commitTransaction() {
        return transactionService.commitTransaction(getConversationId());
    }
    
    @Tool("Cancels the ongoing transaction. This should be used after several unsuccessful try to commit transaction")
    public String cancelTransaction() {
        return transactionService.commitTransaction(getConversationId());
    }

    private String getConversationId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        // Fallback for system processes or unauthenticated contexts
        return "poc-user";
    }
}