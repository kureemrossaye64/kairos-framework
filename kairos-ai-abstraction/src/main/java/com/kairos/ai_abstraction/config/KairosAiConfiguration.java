package com.kairos.ai_abstraction.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Main configuration class for the AI Abstraction module.
 * It enables the VertexAiProperties and scans for service components.
 * The ConditionalOnProperty ensures that this entire configuration is only activated
 * when the 'kairos.ai.vertex.project-id' property is set in the application properties,
_   preventing errors if the application is started without AI configuration.
 */
@Configuration
@ConditionalOnProperty(name = "kairos.ai.vertex.project-id")
@EnableConfigurationProperties(VertexAiProperties.class)
@ComponentScan(basePackages = "com.kairos.ai_abstraction.service.impl")
public class KairosAiConfiguration {
}