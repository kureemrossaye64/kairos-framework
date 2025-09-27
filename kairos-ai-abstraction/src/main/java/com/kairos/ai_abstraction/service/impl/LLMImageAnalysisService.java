package com.kairos.ai_abstraction.service.impl;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.kairos.ai_abstraction.service.ChatLanguageModel;
import com.kairos.ai_abstraction.service.ImageAnalysisService;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class LLMImageAnalysisService implements ImageAnalysisService{
	
	private final ChatLanguageModel transcriptionModel;


    @Override
    public CompletableFuture<String> analyze(String gcsUri) {
        // Use supplyAsync to make the operation non-blocking and conform to the CompletableFuture interface.
        return CompletableFuture.supplyAsync(() -> {
            log.info("Starting transcription with Gemini for GCS URI: {}", gcsUri);

            // 1. Create the multimodal content for the prompt
            TextContent promptText = TextContent.from(
                    "You are an expert image analyst. Describe the contents of this image in detail. \" + \r\n"
                    + "                       \"Include any text visible in the image. Your description will be used for a search index."
            );
            // The MIME type is not strictly needed for GCS URIs as the API can infer it, but it's good practice.
            ImageContent audioContent = ImageContent.from(gcsUri);

            // 2. Construct the user message
            UserMessage userMessage = UserMessage.from(promptText, audioContent);

            // 3. Call the Gemini model
            log.debug("Sending transcription request to Gemini model...");
            AiMessage aiMessage = transcriptionModel.chat(userMessage).aiMessage();
            log.info("Received transcription response from Gemini.");

            // 4. Return the text content
            return aiMessage.text();
        });
    }

}
