package com.kairos.agentic_framework.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Main configuration class for the Agentic Framework module.
 * Enables component scanning to discover the AgentFactory and any @KairosTool beans.
 */
@Configuration
@ComponentScan(basePackages = "com.kairos.agentic_framework")
public class KairosAgenticConfiguration {
}