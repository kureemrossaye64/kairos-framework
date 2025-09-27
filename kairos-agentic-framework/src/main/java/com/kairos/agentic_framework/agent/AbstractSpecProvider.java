package com.kairos.agentic_framework.agent;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationContext;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.ToolMemoryId;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.internal.JsonSchemaElementUtils;
import dev.langchain4j.internal.JsonSchemaElementUtils.VisitedClassMetadata;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSpecProvider  {

	protected ApplicationContext ctx;

	public AbstractSpecProvider(ApplicationContext ctx) {
		super();
		this.ctx = ctx;
	}

	public abstract String getEnrichedDescription();

	public abstract Method getMethodToEnrich();

	public abstract Class<?> getAnnotatedToolClass();

	public Map<ToolSpecification, ToolExecutor> getSpecification() {
		// 1. Scan the classpath to find all conversationally-enabled entities.
		String description = getEnrichedDescription();

		log.info("Dynamically discovered conversational entities: {}", description);

		Method m = getMethodToEnrich();
		Map<ToolSpecification, ToolExecutor> result = new HashMap<ToolSpecification, ToolExecutor>();
		String name = m.getName();
		processToolMethod(ctx, m, result, name, description);
		return result;
	}

	private ToolExecutor createToolExecutor(ApplicationContext ctx, Method method) {
		Object object = ctx.getBean(getAnnotatedToolClass());
		return DefaultToolExecutor.builder().object(object).originalMethod(method).methodToInvoke(method)
				.wrapToolArgumentsExceptions(true).propagateToolExecutionExceptions(true).build();
	}

	private void processToolMethod(ApplicationContext ctx, Method method, Map<ToolSpecification, ToolExecutor> specs,
			String name, String description) {
		ToolSpecification toolSpecification = toolSpecificationFrom(method, name, description);

		ToolExecutor toolExecutor = createToolExecutor(ctx, method);

		specs.put(toolSpecification, toolExecutor);

	}

	private ToolSpecification toolSpecificationFrom(Method method, String name, String description) {
		if (description.isEmpty()) {
			description = null;
		}

		JsonObjectSchema parameters = parametersFrom(method.getParameters());

		return ToolSpecification.builder().name(name).description(description).parameters(parameters).build();
	}

	private JsonObjectSchema parametersFrom(Parameter[] parameters) {

		Map<String, JsonSchemaElement> properties = new LinkedHashMap<>();
		List<String> required = new ArrayList<>();

		Map<Class<?>, VisitedClassMetadata> visited = new LinkedHashMap<>();

		for (Parameter parameter : parameters) {
			if (parameter.isAnnotationPresent(ToolMemoryId.class)) {
				continue;
			}

			boolean isRequired = Optional.ofNullable(parameter.getAnnotation(P.class)).map(P::required).orElse(true);

			properties.put(parameter.getName(), jsonSchemaElementFrom(parameter, visited));
			if (isRequired) {
				required.add(parameter.getName());
			}
		}

		Map<String, JsonSchemaElement> definitions = new LinkedHashMap<>();
		visited.forEach((clazz, visitedClassMetadata) -> {
			if (visitedClassMetadata.recursionDetected) {
				definitions.put(visitedClassMetadata.reference, visitedClassMetadata.jsonSchemaElement);
			}
		});

		if (properties.isEmpty()) {
			return null;
		}

		return JsonObjectSchema.builder().addProperties(properties).required(required)
				.definitions(definitions.isEmpty() ? null : definitions).build();
	}

	private JsonSchemaElement jsonSchemaElementFrom(Parameter parameter, Map<Class<?>, VisitedClassMetadata> visited) {
		P annotation = parameter.getAnnotation(P.class);
		String description = annotation == null ? null : annotation.value();
		return JsonSchemaElementUtils.jsonSchemaElementFrom(parameter.getType(), parameter.getParameterizedType(),
				description, true, visited);
	}


}
