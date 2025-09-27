package com.kairos.agentic_framework.agent;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AxiomDrivenExecutiveAgent implements AiAgent {

    private final ChatModel chatLanguageModel;
    private final ChatMemory chatMemory;
    private final String systemPrompt;
 //   private final InvestigativeAgent investigativeAgent;
 //   private final GuardianAgent guardianAgent;

    @Override
    public String chat(String userMessage) {
       /* log.debug("ARE Executive: Received user message: '{}'", userMessage);
        chatMemory.add(UserMessage.from(userMessage));

        // 1. INVESTIGATION: Delegate to the Investigative Agent to find provisional information.
        log.debug("ARE Executive: Delegating to Investigative Agent.");
        String provisionalProposition = investigativeAgent.investigate(chatMemory.id(), userMessage);
        log.info("ARE Executive: Received provisional proposition from investigator: '{}'", provisionalProposition);

        if (provisionalProposition == null || provisionalProposition.isBlank()) {
            return "I'm sorry, I was unable to find any relevant information for your request.";
        }*/

        // 2. VERIFICATION: Send the provisional result to the Guardian.
    /*    log.debug("ARE Executive: Delegating to Guardian Agent for verification.");
        ContradictionStatus status = guardianAgent.verify(provisionalProposition);
        log.info("ARE Executive: Received Guardian judgment: {}", status);

        if (status == ContradictionStatus.CONTRADICTS_AXIOM) {
            String safeResponse = "I'm sorry, but fulfilling that request might lead to an action that violates a core safety or fairness principle. Could I help with something else in a different way?";
            chatMemory.add(AiMessage.from(safeResponse));
            return safeResponse;
        }
 dsfsdfsdf
        // 3. SYNTHESIS: If verified, ask the LLM to formulate a final, user-facing response.
        log.debug("ARE Executive: Synthesizing final response.");
        // We construct a specific prompt for the synthesis step, feeding the verified info as context.
        String synthesisPrompt = String.format(
            "Based on the user's ongoing conversation and the following newly verified information, formulate a final, helpful, and friendly response. Do not mention that the information was 'verified'; just state it naturally. Verified Information: \"%s\"",
            provisionalProposition
        );
        
        // Use a temporary user message for the synthesis step.
        chatMemory.add(UserMessage.from(synthesisPrompt));

        // Inject the main system prompt ("Kaya" persona) for the final generation.
        chatMemory.messages().add(0, SystemMessage.from(systemPrompt));
        
        AiMessage finalResponse = chatLanguageModel.chat(chatMemory.messages()).aiMessage();
        
        // IMPORTANT: Clean up the temporary messages from memory to not confuse the next turn.
        chatMemory.messages().remove(chatMemory.messages().size() - 1); // Remove synthesis prompt
        chatMemory.messages().remove(0); // Remove system prompt
        chatMemory.add(finalResponse); // Add the final, clean response

        return finalResponse.text();*/
        return "";
    }
}