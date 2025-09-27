package com.kairos.ai_abstraction.service.impl;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kairos.ai_abstraction.service.ChatLanguageModel;
import com.kairos.ai_abstraction.service.TextAnalysisService;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LLMTextAnalysisService implements TextAnalysisService{
	
	
	private  ChatLanguageModel chatModel;
	
	private TextAnalysisAgent agent;
	
	@Autowired
	public LLMTextAnalysisService(ChatLanguageModel chatModel) {
		super();
		this.chatModel = chatModel;
		this.agent = AiServices.create(TextAnalysisAgent.class, chatModel);
	}


	interface TextAnalysisAgent {

        @SystemMessage("You have 3 tasks. "
        		+ "1. Generate a single, concise sentence that summarizes the user input text.\r\n "
        		+ "2. Analyze the user input text and extract the 3 to 5 most relevant topics or keywords. Respond with a comma-separated list ONLY. Example: topic1,topic2,topic3 \r\n"
        		+ "3. Analyze the following text. Generate up to 3 different questions that this text could be a good answer for. Respond with each question on a new line. Do not number them.")
        TextAnalysisResult analyze(@dev.langchain4j.service.UserMessage String text);

    }


	@Override
	public CompletableFuture<TextAnalysisResult> analyze(String text) {
		return CompletableFuture.supplyAsync(() -> {
            

            // 3. Call the Gemini model
            log.debug("Sending text analysis request to LLM model...");
            TextAnalysisResult result = agent.analyze(text);
            log.info("Received text analysis response from LLM.");

            // 4. Return the text content
            return result;
        });
	}

	

}
