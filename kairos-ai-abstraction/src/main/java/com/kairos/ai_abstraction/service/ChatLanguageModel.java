package com.kairos.ai_abstraction.service;

import dev.langchain4j.model.chat.ChatModel;

/**
 * Defines the contract for a chat-based language model service.
 * This interface decouples the application from the specific implementation (e.g., Vertex AI, OpenAI).
 *
 * Design choice: Instead of creating a completely new interface, we extend LangChain4j's
 * interface. This gives us the benefit of abstraction while allowing us to seamlessly
 * plug our implementation into other LangChain4j components like AiServices later.
 */
public interface ChatLanguageModel extends ChatModel {
    // By extending LangChain4j's interface, we inherit methods like:
    // Response<AiMessage> generate(List<ChatMessage> messages);
    // String generate(String userMessage);
    // We don't need to define any new methods for now.
}