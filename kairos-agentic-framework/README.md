# KAIROS - Agentic Framework

The `kairos-agentic-framework` module provides the core components for building and managing conversational AI agents within the Kairos ecosystem. It is designed to be a flexible and extensible framework that leverages the power of large language models (LLMs) to create intelligent, tool-augmented agents.

## Key Features

- **Agent Abstraction:** Defines a simple and clean `AiAgent` interface that standardizes how the application interacts with conversational agents, decoupling the core application logic from the underlying AI implementation.
- **Agent Factory:** Provides a powerful `AgentFactory` that simplifies the creation of `AiAgent` instances. The factory automatically discovers and injects all available `@KairosTool` beans from the Spring application context, making it easy to extend the agent's capabilities.
- **Memory Management:** Each agent instance is provisioned with its own chat memory, ensuring that conversations are stateful and context-aware. It uses a sliding window memory (`MessageWindowChatMemory`) to manage conversational history efficiently.
- **System Prompts:** Allows for the creation of specialized agents with custom personas and instructions by providing system prompts during agent creation.
- **Advanced Agent Architectures (Experimental):** Includes an experimental implementation of a multi-agent system, the **Axiom-Driven Reasoning Engine (ARE)**. This advanced architecture features an `AxiomDrivenExecutiveAgent` that orchestrates a council of specialized agents, including an `InvestigativeAgent` and a `GuardianAgent`, to ensure that responses are not only accurate but also aligned with a core set of ethical axioms.

## Core Components

- **`AiAgent`:** The central interface for all conversational agents.
- **`AgentFactory`:** A Spring service responsible for assembling and configuring `AiAgent` instances with the necessary dependencies, such as the chat model, tools, and memory.
- **`@KairosTool`:** A custom annotation used to mark any Spring bean as a tool that can be used by an AI agent.
- **`AxiomDrivenExecutiveAgent`:** An experimental agent that demonstrates a sophisticated multi-agent approach to reasoning and response generation, ensuring safety and alignment with predefined principles.

## Dependencies

This module relies on several other modules within the Kairos Framework:

- **`kairos-core`:** For core application utilities and data structures.
- **`kairos-ai-abstraction`:** To provide the underlying `ChatLanguageModel` for interacting with the LLM.
- **`kairos-vector-search`:** For enabling agents to perform semantic searches over a knowledge base.
- **`kairos-are-core`:** When the Axiom-Driven Reasoning Engine is enabled, this module provides the `GuardianAgent` and the constitutional principles for response verification.

It also integrates with the following key external libraries:

- **LangChain4j:** For its core AI service abstractions, tool handling, and memory management.
- **Spring Boot:** For dependency injection, component scanning, and overall application configuration.

This module serves as the brain of the Kairos Framework, orchestrating the various components to deliver intelligent and reliable AI-driven interactions.