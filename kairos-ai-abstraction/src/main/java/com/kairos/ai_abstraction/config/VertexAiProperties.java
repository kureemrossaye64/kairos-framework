package com.kairos.ai_abstraction.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * A type-safe configuration class to hold all properties related to Google Vertex AI.
 * This centralizes configuration and provides validation.
 * The prefix "kairos.ai.vertex" will be used in application.yml.
 */
@ConfigurationProperties(prefix = "kairos.ai.vertex")
@Validated
@Getter
@Setter
public class VertexAiProperties {
	
	private String transcriptionProvider = "speech-to-text";

    @NotBlank
    private String projectId;

    @NotBlank
    private String location;

    @NotBlank
    private String chatModelName = "gemini-2.0-flash";
    
    @NotBlank
    private String transcriptionModelName = "gemini-2.0-flash";

    @NotBlank
    private String embeddingModelName = "textembedding-gecko@003";

    private Integer maxOutputTokens = 1024;
    private Double temperature = 0.4;
    private Double topP = 0.95;
    private Integer topK = 40;
}