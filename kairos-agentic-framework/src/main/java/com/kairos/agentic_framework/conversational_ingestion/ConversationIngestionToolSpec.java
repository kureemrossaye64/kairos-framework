package com.kairos.agentic_framework.conversational_ingestion;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.springframework.context.ApplicationContext;

import com.kairos.agentic_framework.agent.AbstractSpecProvider;
import com.kairos.agentic_framework.conversational_ingestion.annotations.ConversationalEntity;

public class ConversationIngestionToolSpec extends AbstractSpecProvider {

	public ConversationIngestionToolSpec(ApplicationContext ctx) {
		super(ctx);
	}

	public String getEnrichedDescription() {
		Reflections reflections = new Reflections("com.kairos");
		Set<Class<?>> entities = reflections.getTypesAnnotatedWith(ConversationalEntity.class);
		String dynamicEntityList = entities.stream().map(cls -> {
			ConversationalEntity annotation = cls.getAnnotation(ConversationalEntity.class);
			return String.format("'%s' (%s)", annotation.name(), annotation.description());
		}).collect(Collectors.joining(", "));

		return "Starts a guided conversation make a registration. Use this when a user expresses a desire to add new information to the platform. Available entity types are: "
				+ dynamicEntityList;
	}

	public Method getMethodToEnrich() {
		Method[] methods = ConversationalIngestionTool.class.getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().equals("startIngestion")) {
				return m;
			}
		}

		return null;
	}

	@Override
	public Class<?> getAnnotatedToolClass() {
		return ConversationalIngestionTool.class;
	}

}
