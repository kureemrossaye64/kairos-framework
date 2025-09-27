package com.kairos.are_core.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

/**
 * An AiService agent responsible for using tools to find raw, provisional information.
 * It is designed to be called by the Executive Agent.
 */
public interface InvestigativeAgent {
    String investigate(@MemoryId Object session, @UserMessage String userMessage);
}