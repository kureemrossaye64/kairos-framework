package com.kairos.ai_abstraction.service.impl;

import org.springframework.stereotype.Service;

import com.kairos.ai_abstraction.config.VertexAiProperties;
import com.kairos.ai_abstraction.service.EmbeddingModel;

import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;

/**
 * Concrete implementation of EmbeddingModel using Google's Vertex AI embedding models.
 */
@Service
public class VertexAiEmbeddingModelImpl extends BgeSmallEnV15QuantizedEmbeddingModel implements EmbeddingModel {

    public VertexAiEmbeddingModelImpl(VertexAiProperties props) {
        super();
    }
}