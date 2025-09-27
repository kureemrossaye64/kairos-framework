package com.kairos.ai_abstraction.service;

import java.util.concurrent.CompletableFuture;

import dev.langchain4j.model.output.structured.Description;

public interface TextAnalysisService {
	
	public CompletableFuture<TextAnalysisResult> analyze(String text);
	
	
	public record TextAnalysisResult(
    		@Description("concise sentence that summarizes the following text")
    		String summary, 
    		@Description("3 to 5 most relevant topics or keywords comma seperated. Example: topic1,topic2,topic3")
    		String topics, 
    		
    		@Description("Generate up to 3 different questions that this text could be a good answer for. Respond with each question on a new line. Do not number them.")
    		String hypotheticalQuestions) {
    	
    }

}
