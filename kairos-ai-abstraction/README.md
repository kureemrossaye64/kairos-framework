# KAIROS - AI Abstraction

The `kairos-ai-abstraction` module serves as a crucial layer in the Kairos Framework, responsible for abstracting all interactions with external AI providers. Its primary goal is to decouple the core application logic from the specific implementations of AI models, allowing for greater flexibility and easier integration with different vendors.

## Key Features

- **Provider Agnosticism:** By defining a set of common interfaces for various AI services (e.g., chat, embedding, transcription), this module allows the rest of the application to remain agnostic about the underlying AI provider. Currently, it provides a concrete implementation for **Google Vertex AI**.
- **Seamless LangChain4j Integration:** The service interfaces in this module, such as `ChatLanguageModel`, are designed to extend the core interfaces from the **LangChain4j** library. This clever design choice allows Kairos's custom AI services to be used interchangeably within the LangChain4j ecosystem, enabling seamless integration with components like `AiServices`.
- **Type-Safe Configuration:** It provides a robust and type-safe configuration mechanism through the `VertexAiProperties` class, which centralizes all connection and model-specific settings for Google Vertex AI. This ensures that all required properties are validated at startup.
- **Conditional Activation:** The entire AI configuration is conditionally activated based on the presence of a `project-id` in the application properties. This allows the application to run without an active AI backend for development or testing purposes.
- **Extensible Service Layer:** The module is organized into a clean `service` package containing interfaces for various AI functionalities (`ChatLanguageModel`, `EmbeddingModel`, `AudioTranscriptionService`, etc.) and an `impl` package for their concrete implementations. This makes it straightforward to add new AI providers in the future.

## Core Components

- **`ChatLanguageModel`:** The primary interface for interacting with a chat-based large language model. It extends `dev.langchain4j.model.chat.ChatModel`.
- **`VertexAiGeminiChatModelImpl`:** The concrete implementation of `ChatLanguageModel`, configured to work with Google's Gemini models via Vertex AI.
- **`VertexAiProperties`:** A `@ConfigurationProperties` class that holds all the necessary settings for connecting to Google Vertex AI, such as project ID, location, model names, and generation parameters.
- **`KairosAiConfiguration`:** The main Spring configuration class that enables the type-safe properties and scans for service implementations.

## Dependencies

This module integrates with the following key libraries:

- **LangChain4j:** For its core AI model interfaces and the specific `langchain4j-vertex-ai` and `langchain4j-vertex-ai-gemini` integrations.
- **Spring Boot:** For dependency injection, configuration management, and conditional bean loading.

By providing this abstraction layer, the `kairos-ai-abstraction` module ensures that the Kairos Framework remains adaptable to the rapidly evolving landscape of AI providers and models.