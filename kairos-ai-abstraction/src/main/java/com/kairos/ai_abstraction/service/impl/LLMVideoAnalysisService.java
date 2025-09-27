package com.kairos.ai_abstraction.service.impl;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.kairos.ai_abstraction.service.ChatLanguageModel;
import com.kairos.ai_abstraction.service.VideoAnalysisService;

import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.VideoContent;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LLMVideoAnalysisService implements VideoAnalysisService{
	
	private final ChatLanguageModel chatModel;
	
	private final VideoAgent videoAgent;
	
	public LLMVideoAnalysisService(ChatLanguageModel chatModel) {
		super();
		this.chatModel = chatModel;
		this.videoAgent = AiServices.create(VideoAgent.class, chatModel);
	}
	
	interface VideoAgent {
        
        VideoAnalysisResult describe(UserMessage message);
    }

	@Override
	public CompletableFuture<VideoAnalysisResult> analyze(String storageURI) {
		return CompletableFuture.supplyAsync(() -> {
            log.info("Starting transcription with Gemini for GCS URI: {}", storageURI);

            // 1. Create the multimodal content for the prompt
            TextContent prompt = TextContent.from(
                    "You are an expert multimedia analyst. Analyze the following video. " +
                    "Your task is to provide two things in a structured JSON format: " +
                    "1. A full, accurate 'transcript' of all spoken words. " +
                    "2. A concise 'visualNarrative' describing the key scenes and actions that occur. " +
                    "3. The audio can be in Mauritian Creole, French, English or Arabic " +
                    "Do not add any other commentary. Your output must be only the JSON object."
                );
            // The MIME type is not strictly needed for GCS URIs as the API can infer it, but it's good practice.
            VideoContent videoContent = VideoContent.from(storageURI);

            // 2. Construct the user message
            UserMessage userMessage = UserMessage.from(prompt,videoContent);

            // 3. Call the Gemini model
            log.debug("Sending transcription request to Gemini model...");
            VideoAnalysisResult result = videoAgent.describe(userMessage);
            log.info("Received transcription response from Gemini.");

            // 4. Return the text content
            return result;
        });
	}

	
	
	

}
