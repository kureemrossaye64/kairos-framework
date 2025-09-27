package com.kairos.ai_abstraction.service.impl;

import java.util.concurrent.CompletableFuture;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.kairos.ai_abstraction.config.VertexAiProperties;
import com.kairos.ai_abstraction.service.ChatLanguageModel;
import com.kairos.ai_abstraction.service.AudioTranscriptionService;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A TranscriptionService implementation that uses a multimodal Gemini model for transcription.
 * This service is activated when 'kairos.ai.vertex.transcription-provider' is set to 'gemini'.
 */
@Service
@Slf4j
@AllArgsConstructor
public class LLMAudioTranscriptionServiceImpl implements AudioTranscriptionService {

    private final ChatLanguageModel transcriptionModel;


    @Override
    public CompletableFuture<String> transcribe(String gcsUri) {
        // Use supplyAsync to make the operation non-blocking and conform to the CompletableFuture interface.
        return CompletableFuture.supplyAsync(() -> {
            log.info("Starting transcription with Gemini for GCS URI: {}", gcsUri);

            // 1. Create the multimodal content for the prompt
            TextContent promptText = TextContent.from(
                    "You are an expert audio transcriber. Transcribe the following audio file accurately. " +
                    "The audio is in Mauritian Creole and may contain singing. " +
                    "Provide only the transcribed text, with no additional commentary or formatting."
            );
            // The MIME type is not strictly needed for GCS URIs as the API can infer it, but it's good practice.
            AudioContent audioContent = AudioContent.from(gcsUri);

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