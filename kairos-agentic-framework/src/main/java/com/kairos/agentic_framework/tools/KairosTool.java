package com.kairos.agentic_framework.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * A meta-annotation that combines LangChain4j's @Tool with Spring's @Component.
 *
 * By annotating a class with @KairosTool, it will be automatically:
 * 1. Recognized as a Spring bean, making it eligible for dependency injection.
 * 2. Identified as a collection of tools that can be provided to an AI agent.
 *
 * Example Usage:
 * <pre>
 * {@code
 * @KairosTool
 * public class CalculatorTools {
 *
 *     @Tool("Calculates the sum of two numbers")
 *     public int add(int a, int b) {
 *         return a + b;
 *     }
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface KairosTool {
}