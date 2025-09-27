package com.kairos.agentic_framework.governance;

/**
 * Strategy interface for a single type of analysis Kaya can perform on chat logs.
 */
public interface InsightModule {
    /** The user-friendly name of the analysis module. */
    String getModuleName();
    /** The detailed instruction prompt for the LLM to perform the analysis. */
    String getSystemPromptForAnalysis();
    /** A method to post-process or format the AI's raw analysis text. */
    String processAnalysisResult(String analysisText);
}