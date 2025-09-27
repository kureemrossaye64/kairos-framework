package com.kairos.agentic_framework.agent;

/**
 * A high-level interface representing a conversational AI Agent.
 * This is the primary interface that application services will interact with.
 *
 * It is essentially a strongly-typed wrapper around LangChain4j's AiServices,
 * providing a clear contract for agent interaction.
 */
public interface AiAgent {

    /**
     * Engages in a conversation with the agent.
     *
     * @param message The user's message.
     * @return The agent's response.
     */
    String chat(String message);
}