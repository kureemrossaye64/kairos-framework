package com.kairos.agentic_framework.conversational_ingestion;

import com.kairos.agentic_framework.config.ConversationIdProvider;
import com.kairos.agentic_framework.tools.KairosTool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@KairosTool
@RequiredArgsConstructor
@Slf4j
public class ConversationalIngestionTool  {

	private final GenericIngestionService ingestionService;

	private final ConversationIdProvider conversationIdProvider;

	// Define a constant for our signal prefix.
	public static final String INGESTION_SIGNAL = "KAYA_INGESTION_FLOW::";

	// @Tool("Starts a guided conversation to register a new entity, such as a
	// 'Partner', 'Team','Booking' , or 'Event'. Use this when a user expresses a
	// desire to add new information to the platform.")
	public String startIngestion(
			@P("The type of entity to register. Should be one of the known types like 'Partner'.") String entityName,
			@P("Set to 'true' to generate a proposal for the user explaining the benefits of registration without starting it. Set to 'false' ONLY after the user has explicitly agreed to proceed.") boolean dryRun) {
		String conversationId = getConversationId();
		String firstQuestion = ingestionService.startIngestion(conversationId, entityName, dryRun);
		return INGESTION_SIGNAL + firstQuestion;
	}

	@Tool("When the user provides an answer to a question")
	public String provideAnswer(@P("The answer provided by the user") String answer) {
		String conversationId = getConversationId();
		String firstQuestion = ingestionService.processAnswer(conversationId, answer);
		return INGESTION_SIGNAL + firstQuestion;
	}

	@Tool("When the user makes a correction after viewing summary and provides an new answer")
	public String correctAnswer(@P("The correct answer provided by the user") String answer,
			@P("The incorrect answer previously provided by the user") String incorrectAnswer) {
		String conversationId = getConversationId();
		String firstQuestion = ingestionService.correctAnswer(conversationId, answer, incorrectAnswer);
		return INGESTION_SIGNAL + firstQuestion;
	}

	@Tool("Confirms and saves the information after a summary has been presented and the user has agreed. Only use this tool when the user explicitly confirms (e.g., says 'yes', 'correct', 'looks good').")
	public String confirmAndSave() {
		return ingestionService.confirmAndSave(getConversationId());
	}

	private String getConversationId() {
		return conversationIdProvider.getConversationId();
	}

	

}