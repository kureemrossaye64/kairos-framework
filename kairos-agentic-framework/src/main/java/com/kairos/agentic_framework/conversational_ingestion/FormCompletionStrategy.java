package com.kairos.agentic_framework.conversational_ingestion;

import com.kairos.agentic_framework.conversational_ingestion.form.Form;

/**
 * A strategy interface for defining the actions to be taken
 * once a conversational form for a specific entity has been successfully completed.
 *
 * This is an implementation of the Strategy Design Pattern.
 */
public interface FormCompletionStrategy {

    /**
     * Returns the user-friendly name of the entity this strategy handles.
     * This name MUST match the 'name' attribute in the @ConversationalEntity annotation.
     * @return The name of the entity (e.g., "Partner", "Team").
     */
    String getEntityName();

    /**
     * The core logic to be executed when the form is complete.
     * This method will contain the domain-specific actions, such as
     * creating entities, saving to the database, and triggering other services.
     * @param completedForm The Form object containing all the user-provided answers.
     */
    void execute(Form completedForm);
}