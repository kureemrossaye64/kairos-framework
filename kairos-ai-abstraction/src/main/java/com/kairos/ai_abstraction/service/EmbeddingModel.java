package com.kairos.ai_abstraction.service;



/**
 * Defines the contract for a text embedding model service.
 * This interface decoules our application from the specific provider.
 * Like the ChatLanguageModel, we extend the LangChain4j interface for compatibility.
 */
public interface EmbeddingModel extends dev.langchain4j.model.embedding.EmbeddingModel {
    // By extending LangChain4j's interface, we inherit methods like:
    // Response<Embedding> embed(String text);
    // Response<List<Embedding>> embedAll(List<String> texts);
}